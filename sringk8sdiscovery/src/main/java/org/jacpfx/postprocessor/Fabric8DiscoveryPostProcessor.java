package org.jacpfx.postprocessor;

import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.util.KubeClientBuilder;
import org.jacpfx.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by amo on 05.04.17.
 */
public class Fabric8DiscoveryPostProcessor implements BeanPostProcessor {

  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";
  private static final String DEFAULT_NAMESPACE = "default";
  public static final String SEPERATOR = ":";
  private final String api_token, master_url, namespace;
  private Logger logger = Logger.getLogger(Fabric8DiscoveryPostProcessor.class.getName());

  public Fabric8DiscoveryPostProcessor() {
    api_token = null;
    master_url = DEFAULT_MASTER_URL;
    namespace = DEFAULT_NAMESPACE;
  }

  private Fabric8DiscoveryPostProcessor(String api_token, String master_url,String namespace) {
    this.master_url = master_url!=null?master_url:DEFAULT_MASTER_URL;
    this.api_token = api_token;
    this.namespace = namespace!=null?namespace:DEFAULT_NAMESPACE;
  }

  public static Fabric8DiscoveryPostProcessor builder() {
    return new Fabric8DiscoveryPostProcessor();
  }

  public Fabric8DiscoveryPostProcessor apiToken(String api_token) {
    return new Fabric8DiscoveryPostProcessor(api_token,master_url,namespace);
  }

  public Fabric8DiscoveryPostProcessor masterUrl(String master_url) {
    return new Fabric8DiscoveryPostProcessor(api_token,master_url,namespace);
  }

  public Fabric8DiscoveryPostProcessor namespace(String namespace) {
    return new Fabric8DiscoveryPostProcessor(api_token,master_url,namespace);
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    resolveServiceNameAnnotation(bean);
    // TODO resolveEndpointAnnotation
    return bean;
  }

  private void resolveServiceNameAnnotation(Object bean) {
    final List<Field> serverNameFields = Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
        .collect(Collectors.toList());

    if (!serverNameFields.isEmpty()) {
      KubernetesClient client = KubeClientBuilder.buildeKubernetesClient(api_token, master_url);
      if (client != null) {
        serverNameFields.forEach(serviceNameField -> {
          final ServiceName serviceNameAnnotation = serviceNameField
              .getAnnotation(ServiceName.class);
          final String serviceName = serviceNameAnnotation.value();
          final Optional<Service> serviceEntryOptional = client
              .services()
              .list()
              .getItems()
              .stream()
              .filter(item -> item.getMetadata().getNamespace().equalsIgnoreCase(namespace))
              .filter(item -> item.getMetadata().getName().equalsIgnoreCase(serviceName))
              .findFirst();

          serviceEntryOptional.ifPresent(serviceEntry -> {
            String hostString = getHostString(serviceEntry);
            setFieldValue(bean, serviceNameField, hostString);
          });
        });

      } else {
        logger.info("no Kubernetes client available");
      }

    }
  }

  private void setFieldValue(Object bean, Field serviceNameField, String hostString) {
    serviceNameField.setAccessible(true);
    try {
      serviceNameField.set(bean, hostString);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private String getHostString(Service serviceEntry) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (!ports.isEmpty()) {
      hostString = clusterIP + SEPERATOR + ports.get(0).getPort();
    }
    return hostString;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }


}
