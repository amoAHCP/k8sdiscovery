package org.jacpfx.util;

import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jacpfx.discovery.Endpoints;
import org.jacpfx.discovery.Pods;
import org.jacpfx.discovery.annotation.Label;

/**
 * Created by amo on 13.04.17.
 */
public class ServiceUtil {

  public static final String SEPERATOR = ":";
  private static final Logger logger = Logger.getLogger(ServiceUtil.class.getName());

  public static void resolveK8SAnnotationsAndInit(
      Object bean,
      String user,
      String pwd,
      String api_token,
      String master_url,
      String namespace,
      KubernetesClient clientPassed) {
    final List<Field> serverNameFields = ServiceUtil.findServiceFields(bean);
    final List<Field> labelFields = ServiceUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty()) {
      KubernetesClient client = getKubernetesClient(
          user,
          pwd,
          api_token,
          master_url,
          namespace,
          clientPassed);
      if (client != null) {
        ServiceUtil
            .findServiceEntryAndSetValue(bean, serverNameFields, client);
      } else {
        logger.info("no Kubernetes client available");
      }

    }

    if (!labelFields.isEmpty()) {
      KubernetesClient client = getKubernetesClient(
          user,
          pwd,
          api_token,
          master_url,
          namespace,
          clientPassed);
      if (client != null) {
        ServiceUtil.findLabelAndSetValue(bean, labelFields, client);
      } else {
        logger.info("no Kubernetes client available");
      }

    }
  }

  private static KubernetesClient getKubernetesClient(String user, String pwd, String api_token,
      String master_url, String namespace, KubernetesClient clientPassed) {
    return clientPassed != null ? clientPassed
        : KubeClientBuilder.buildKubernetesClient(user, pwd, api_token, master_url, namespace);
  }

  public static void findServiceEntryAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      final ServiceName serviceNameAnnotation = serviceNameField
          .getAnnotation(ServiceName.class);
      final String serviceName = serviceNameAnnotation.value();
      final Optional<Service> serviceEntryOptional = findServiceEntry(client, serviceName);
      serviceEntryOptional.ifPresent(serviceEntry -> {
        String hostString = getHostString(serviceEntry);
        FieldUtil.setFieldValue(bean, serviceNameField, hostString);
      });
    });
  }

  public static void findLabelAndSetValue(Object bean, List<Field> serverNameFields,
      KubernetesClient client) {
    Objects.requireNonNull(client, "no client available");
    serverNameFields.forEach(serviceNameField -> {
      final Label serviceNameAnnotation = serviceNameField
          .getAnnotation(Label.class);
      final String labelName = serviceNameAnnotation.name();
      final String labelValue = serviceNameAnnotation.labelValue();

      setEndpoints(bean, client, serviceNameField, labelName, labelValue);

      setPods(bean, client, serviceNameField, labelName, labelValue);

    });
  }

  private static void setPods(Object bean, KubernetesClient client,
      Field serviceNameField, String labelName, String labelValue) {
    Objects.requireNonNull(client, "no client available");
    if (serviceNameField.getType().isAssignableFrom(Pods.class)) {
      Pods pods = Pods.build().client(client).namespace(client.getNamespace())
          .labelName(labelName).labelValue(labelValue);
      FieldUtil.setFieldValue(bean, serviceNameField, pods);
    }
  }

  private static void setEndpoints(Object bean, KubernetesClient client,
      Field serviceNameField, String labelName, String labelValue) {
    Objects.requireNonNull(client, "no client available");
    if (serviceNameField.getType().isAssignableFrom(Endpoints.class)) {
      Endpoints endpoint = Endpoints.build().client(client).namespace(client.getNamespace())
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

  public static Optional<Service> findServiceEntry(KubernetesClient client, String serviceName) {
    Objects.requireNonNull(client, "no client available");
    return Optional.ofNullable(client
        .services()
        .inNamespace(client.getNamespace())
        .list())
        .orElse(new ServiceList())
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
