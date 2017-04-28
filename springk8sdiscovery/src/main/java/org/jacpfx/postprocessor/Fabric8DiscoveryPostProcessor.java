package org.jacpfx.postprocessor;

import java.util.logging.Logger;
import org.jacpfx.util.ServiceUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by amo on 05.04.17.
 */
public class Fabric8DiscoveryPostProcessor implements BeanPostProcessor {

  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";
  private static final String DEFAULT_NAMESPACE = "default";

  private final String api_token, master_url, namespace;
  private Logger logger = Logger.getLogger(Fabric8DiscoveryPostProcessor.class.getName());

  public Fabric8DiscoveryPostProcessor() {
    api_token = null;
    master_url = DEFAULT_MASTER_URL;
    namespace = DEFAULT_NAMESPACE;
  }

  private Fabric8DiscoveryPostProcessor(String api_token, String master_url, String namespace) {
    this.master_url = master_url != null ? master_url : DEFAULT_MASTER_URL;
    this.api_token = api_token;
    this.namespace = namespace != null ? namespace : DEFAULT_NAMESPACE;
  }

  public static Fabric8DiscoveryPostProcessor builder() {
    return new Fabric8DiscoveryPostProcessor();
  }

  public Fabric8DiscoveryPostProcessor apiToken(String api_token) {
    return new Fabric8DiscoveryPostProcessor(api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor masterUrl(String master_url) {
    return new Fabric8DiscoveryPostProcessor(api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor namespace(String namespace) {
    return new Fabric8DiscoveryPostProcessor(api_token, master_url, namespace);
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    ServiceUtil.resolveK8SAnnotationsAndInit(bean,api_token,master_url,namespace,null);
    return bean;
  }



  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }


}
