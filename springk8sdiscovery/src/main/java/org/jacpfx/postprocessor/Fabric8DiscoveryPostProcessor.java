package org.jacpfx.postprocessor;

import org.jacpfx.discovery.DiscoveryUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;

/**
 * Created by amo on 05.04.17.
 */
public class Fabric8DiscoveryPostProcessor implements BeanPostProcessor {

  private final String user, pwd, api_token, master_url, namespace;
  @Autowired
  private Environment env;

  public Fabric8DiscoveryPostProcessor() {
    this.api_token = null;
    this.user = null;
    this.pwd = null;
    this.master_url = null;
    this.namespace = null;
  }

  private Fabric8DiscoveryPostProcessor(String user, String pwd, String api_token,
      String master_url, String namespace) {
    this.master_url = master_url;
    this.api_token = api_token;
    this.user = user;
    this.pwd = pwd;
    this.namespace = namespace;
  }

  public static Fabric8DiscoveryPostProcessor builder() {
    return new Fabric8DiscoveryPostProcessor();
  }

  public Fabric8DiscoveryPostProcessor apiToken(String api_token) {
    return new Fabric8DiscoveryPostProcessor(user, pwd, api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor masterUrl(String master_url) {
    return new Fabric8DiscoveryPostProcessor(user, pwd, api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor namespace(String namespace) {
    return new Fabric8DiscoveryPostProcessor(user, pwd, api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor user(String user) {
    return new Fabric8DiscoveryPostProcessor(user, pwd, api_token, master_url, namespace);
  }

  public Fabric8DiscoveryPostProcessor pwd(String pwd) {
    return new Fabric8DiscoveryPostProcessor(user, pwd, api_token, master_url, namespace);
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    DiscoveryUtil
        .resolveK8SAnnotationsAndInit(bean, user, pwd, api_token, master_url, namespace,  env, null);
    return bean;
  }


  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }


}
