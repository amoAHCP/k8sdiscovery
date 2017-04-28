package org.jacpfx.client;

import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.jacpfx.util.KubeClientBuilder;
import org.jacpfx.util.ServiceUtil;

/**
 * Created by amo on 06.04.17.
 */
public class Fabric8DiscoveryClient {

  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  private static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";
  private static final String DEFAULT_NAMESPACE = "default";
  public static final String SEPERATOR = ":";
  private final String api_token, master_url, namespace;
  private final KubernetesClient client;
  private final Logger logger = Logger.getLogger(Fabric8DiscoveryClient.class.getName());

  public Fabric8DiscoveryClient() {
    api_token = null;
    master_url = DEFAULT_MASTER_URL;
    namespace = DEFAULT_NAMESPACE;
    client = null;
  }

  private Fabric8DiscoveryClient(KubernetesClient client, String api_token, String master_url,
      String namespace) {
    this.master_url = master_url != null ? master_url : DEFAULT_MASTER_URL;
    this.api_token = api_token;
    this.namespace = namespace != null ? namespace : DEFAULT_NAMESPACE;
    this.client = KubeClientBuilder.buildKubernetesClient(api_token, master_url);
  }

  public interface ApiToken {

    MasterUrl apiToken(String token);
  }

  public interface MasterUrl {

    Namespace masterUrl(String masterUrl);
  }

  public interface Namespace {

    Fabric8DiscoveryClient namespace(String namespace);
  }

  public static ApiToken builder() {
    return apitoken -> masterurl -> namespace -> new Fabric8DiscoveryClient(null, apitoken,
        masterurl, namespace);
  }

  public void findServiceByName(String serviceName, Consumer<Service> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<Service> serviceEntryOptional = client
        .services()
        .list()
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getNamespace().equalsIgnoreCase(namespace))
        .filter(item -> item.getMetadata().getName().equalsIgnoreCase(serviceName))
        .findFirst();

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with name " + serviceName + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);

  }

  public void findServiceByLabel(String label, Consumer<Service> serviceConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final Optional<Service> serviceEntryOptional = client
        .services()
        .list()
        .getItems()
        .stream()
        .filter(item -> item.getMetadata().getNamespace().equalsIgnoreCase(namespace))
        .filter(item -> item.getMetadata().getLabels().keySet().stream()
            .filter(key -> key.equalsIgnoreCase(label)).findFirst().isPresent())
        .findFirst();

    if (!serviceEntryOptional.isPresent()) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    serviceEntryOptional.ifPresent(serviceConsumer::accept);
  }

  public void findEndpointsByLabel(String label, Consumer<EndpointsList> endpointsListConsumer,
      Consumer<Throwable> error) {
    Objects.requireNonNull(client, "no client available");
    final EndpointsList serviceEntryOptional = client
        .endpoints()
        .inNamespace(namespace)
        .withLabel(label)
        .list();

    if (serviceEntryOptional == null) {
      error.accept(new Throwable("no service with label " + label + " found"));
    }
    endpointsListConsumer.accept(serviceEntryOptional);
  }


  public void resolveAnnotations(Object bean) {
    Objects.requireNonNull(client, "no client available");
    final List<Field> serverNameFields = ServiceUtil.findServiceFields(bean);
    final List<Field> labelFields = ServiceUtil.findLabelields(bean);
    if (!serverNameFields.isEmpty())
      ServiceUtil.findServiceEntryAndSetValue(bean, serverNameFields, client, namespace);

    if (!labelFields.isEmpty())
      ServiceUtil.findLabelAndSetValue(bean, labelFields, client, namespace);

  }
}
