package org.jacpfx.util;

import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.discovery.Endpoints;
import org.jacpfx.discovery.Label;
import org.jacpfx.discovery.Pods;

/**
 * Created by amo on 13.04.17.
 */
public class ServiceUtil {

  public static final String SEPERATOR = ":";
  private static final Logger logger = Logger.getLogger(ServiceUtil.class.getName());

  public static void resolveK8SAnnotationsAndInit(Object bean, String api_token,String master_url,String namespace, KubernetesClient clientPassed) {
    final List<Field> serverNameFields = ServiceUtil.findServiceFields(bean);
    final List<Field> labelFields = ServiceUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      KubernetesClient client = clientPassed!=null?clientPassed:KubeClientBuilder.buildKubernetesClient(api_token, master_url);
      if (client != null) {
        ServiceUtil.findServiceEntryAndSetValue(bean, serverNameFields, client, namespace);
      } else {
        logger.info("no Kubernetes client available");
      }

    }

    if (!labelFields.isEmpty()) {
      KubernetesClient client = clientPassed!=null?clientPassed:KubeClientBuilder.buildKubernetesClient(api_token, master_url);
      if (client != null) {
        ServiceUtil.findLabelAndSetValue(bean, labelFields, client, namespace);
      } else {
        logger.info("no Kubernetes client available");
      }

    }
  }

  public static void findServiceEntryAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client, String namespace) {
    serverNameFields.forEach(serviceNameField -> {
      final ServiceName serviceNameAnnotation = serviceNameField
          .getAnnotation(ServiceName.class);
      final String serviceName = serviceNameAnnotation.value();
      final Optional<Service> serviceEntryOptional = findServiceEntry(client, serviceName,
          namespace);

      serviceEntryOptional.ifPresent(serviceEntry -> {
        String hostString = getHostString(serviceEntry);
        FieldUtil.setFieldValue(bean, serviceNameField, hostString);
      });
    });
  }

  public static void findLabelAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client, String namespace) {
    serverNameFields.forEach(serviceNameField -> {
      final Label serviceNameAnnotation = serviceNameField
          .getAnnotation(Label.class);
      final String labelName = serviceNameAnnotation.name();
      final String labelValue = serviceNameAnnotation.labelValue();

      setEndpoints(bean, client, namespace, serviceNameField, labelName, labelValue);

      setPods(bean, client, namespace, serviceNameField, labelName, labelValue);

    });
  }

  private static void setPods(Object bean, KubernetesClient client, String namespace,
      Field serviceNameField, String labelName, String labelValue) {
    if (serviceNameField.getType().isAssignableFrom(Pods.class)) {
      Pods pods = Pods.build().client(client).namespace(namespace)
          .labelName(labelName).labelValue(labelValue);
      FieldUtil.setFieldValue(bean, serviceNameField, pods);
    }
  }

  private static void setEndpoints(Object bean, KubernetesClient client, String namespace,
      Field serviceNameField, String labelName, String labelValue) {
    if (serviceNameField.getType().isAssignableFrom(Endpoints.class)) {
      Endpoints endpoint = Endpoints.build().client(client).namespace(namespace)
          .labelName(labelName).labelValue(labelValue);
      FieldUtil.setFieldValue(bean, serviceNameField, endpoint);
    }
  }

  public static List<Field> findServiceFields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(ServiceName.class))
        .collect(Collectors.toList());
  }

  public static List<Field> findLabelields(Object bean) {
    return Stream.of(bean.getClass().getDeclaredFields())
        .filter((Field filed) -> filed.isAnnotationPresent(Label.class))
        .collect(Collectors.toList());
  }

  private static Optional<Service> findServiceEntry(KubernetesClient client, String serviceName,
      String namespace) {
    return client
        .services()
        .inNamespace(namespace)
        .list()
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getName().equalsIgnoreCase(serviceName))
        .findFirst();
  }


  private static String getHostString(Service serviceEntry) {
    String hostString = "";
    final String clusterIP = serviceEntry.getSpec().getClusterIP();
    final List<ServicePort> ports = serviceEntry.getSpec().getPorts();
    if (!ports.isEmpty()) {
      hostString = clusterIP + SEPERATOR + ports.get(0).getPort();
    }
    return hostString;
  }
}
