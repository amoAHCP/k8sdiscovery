package org.jacpfx.discovery;

import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Created by amo on 13.04.17.
 */
public class Endpoints {

  private final KubernetesClient client;
  private final String namespace, labelName, labelValue;

  private Endpoints(KubernetesClient client, String namespace, String labelName,
      String labelValue) {
    this.client = client;
    this.namespace = namespace;
    this.labelName = labelName;
    this.labelValue = labelValue;
  }

  public interface LabelValue {

    Endpoints labelValue(String labelValue);
  }

  public interface LabelName {

    LabelValue labelName(String serviceName);
  }

  public interface Namespace {

    LabelName namespace(String namespace);
  }

  public interface Client {

    Namespace client(KubernetesClient client);
  }

  public static Client build() {
    return client -> namespace -> labelName -> labelValue -> new Endpoints(client, namespace,
        labelName, labelValue);
  }

  public EndpointsList getEndpoints() {
    return labelValue != null && !labelValue.isEmpty() ?
        client.endpoints().inNamespace(namespace).withLabel(labelName, labelValue).list() :
        client.endpoints().inNamespace(namespace).withLabel(labelName).list();
  }

}
