package jacpfx.discovery;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Created by amo on 13.04.17.
 */
public class Pods {

  private final KubernetesClient client;
  private final String namespace, labelName, labelValue;

  private Pods(KubernetesClient client, String namespace, String labelName,
      String labelValue) {
    this.client = client;
    this.namespace = namespace;
    this.labelName = labelName;
    this.labelValue = labelValue;
  }

  public interface LabelValue {

    Pods labelValue(String labelValue);
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
    return client -> namespace -> labelName -> labelValue -> new Pods(client, namespace,
        labelName, labelValue);
  }

  public PodList getPods() {
    return labelValue != null && !labelValue.isEmpty() ?
        client.pods().inNamespace(namespace).withLabel(labelName, labelValue).list() :
        client.pods().inNamespace(namespace).withLabel(labelName).list();
  }

}
