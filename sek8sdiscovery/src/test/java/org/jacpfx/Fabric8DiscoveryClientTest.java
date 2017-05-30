package org.jacpfx;

import static org.powermock.api.mockito.PowerMockito.when;

import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.EndpointSubset;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.jacpfx.client.Fabric8DiscoveryClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by amo on 18.05.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Fabric8DiscoveryClient.class)
public class Fabric8DiscoveryClientTest {
  private static final String SERVICE_NAME = "";
  private static final String SERVICE_LABEL = "theLabel";
  private static final String SERVICE_LABEL_VALUE = "serviceLabelValue";
  private static final String NAMESPACE = "theNamespace";
  private static final String KUBERNETES_MASTER_URL = "http://bla";
  private static final String API_TOKEN = "token";

  @Mock
  private DefaultKubernetesClient client;

  @Mock
  private MixedOperation endpoints;

  @Mock
  private MixedOperation services;

  @Mock
  private MixedOperation inNamespace;

  @Mock
  private MixedOperation withLabel;

  private EndpointsList nodesInNamespace = new EndpointsList();
  private EndpointsList nodesWithLabel = new EndpointsList();

  @Before
  public void setup() throws Exception {
    PowerMockito.whenNew(DefaultKubernetesClient.class).withAnyArguments().thenReturn(client);

    when(client.services()).thenReturn(services);
    when(services.inNamespace(NAMESPACE)).thenReturn(inNamespace);
    when(inNamespace.list()).thenReturn(nodesInNamespace);
    //when(inNamespace.withLabel(SERVICE_LABEL)).thenReturn(withLabel);
    //when(withLabel.list()).thenReturn(nodesWithLabel);
   // when(inNamespace.list()).thenReturn(nodesInNamespace);
  }

  @Test
  @Ignore
  public void testInit(){
    Endpoints discoveryNode = createEndpoints(1);
    nodesInNamespace.getItems().add(discoveryNode);
   // nodesWithLabel.getItems().add(discoveryNode);
    Fabric8DiscoveryClient theclient = new Fabric8DiscoveryClient(client,API_TOKEN,KUBERNETES_MASTER_URL,NAMESPACE);

    theclient.findServicesByLabel(NAMESPACE, result -> {
      System.out.println(result);
    }, error -> {
      System.out.println(error);
    });
  }

  private Endpoints createEndpoints(int id) {
    Endpoints endpoints = new Endpoints();
    EndpointSubset subset = new EndpointSubset();
    endpoints.getSubsets().add(subset);
    EndpointAddress address = new EndpointAddress();
    subset.getAddresses().add(address);
    address.setIp("1.1.1.1");
    address.getAdditionalProperties().put("hazelcast-service-port", String.valueOf(id));
    return endpoints;
  }

}
