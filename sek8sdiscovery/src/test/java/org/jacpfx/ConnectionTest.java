package org.jacpfx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.fabric8.annotations.ServiceName;
import io.fabric8.annotations.WithLabel;
import io.fabric8.annotations.WithLabels;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.jacpfx.client.Fabric8DiscoveryClient;
import org.junit.Test;

public class ConnectionTest {

  final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tODhoOWciLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImNjMjkyYzU3LThlZTctMTFlNy05ZmYwLTlhYzkzMzg0ZTNjMyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmRlZmF1bHQifQ.BfGnC_8w8ac19dFy8nFkq4Rj3K3rG6R_v155sMSOOIZJ-bIdusC-UH8qVzzgI2ZD0Gba56W8Awq2TUrAHuzAhu3cRORQWTT-hGzzJs1jL5imJsnu_hdSUvTtx4d--JYwB8X7kEPSfat3lkNqVrci3nCKKoAZmUmVV7md6VzTrMfoYteYKP67iYwcccNHbjnXROx7h6sTwtsD4TGqLvvkVv3gVMQOinRJVvwsBCirB1hUJE4wFIKjcZA6hEVEbF7j0aESO-6SUvZCQz4y4R6oxrqsUTYWqyUTDOuNuxTsk5N61EqVyMnmhBRhRlfgTv0EwXFJgX1z3ZDu_Meitl77fQ";
  // Start minikube & execute: kubectl proxy // find token by executing: kubectl describe secret
  @Test
  public void testConnection() {
    ConfigBuilder configBuilder = new ConfigBuilder().
        withMasterUrl("http://localhost:8001").
        withNamespace("default");//.withOauthToken(token);
    final Config build = configBuilder.build();
    final DefaultKubernetesClient client = new DefaultKubernetesClient(build);
    assertNotNull(client);
    Stream.of(client.services().list()).forEach(l -> {
      l.getItems().forEach(e -> System.out.println(
          e.getMetadata().getName() + ":" + e.getSpec().getClusterIP() + ":" + e.getSpec()
              .getPorts()));
    });
    System.out.println("---------");
    Stream.of(client.endpoints().list()).forEach(l -> {
      l.getItems().forEach(e -> {
        System.out.println(":::::"+e.getMetadata().getName());
        e.getSubsets().forEach(subset -> {
          subset.getPorts().forEach(ports -> {
            System.out.println("ports: "+ports.getName()+"  : "+ports.getPort());
          });
          subset.getAddresses().forEach(address -> {
            System.out.println(address.getIp()+" : "+address.getAdditionalProperties());
          });
        });
      });
    });

    System.out.println("---------");
    Stream.of(client.pods().list()).forEach(l -> {
      l.getItems().forEach(e -> {
        System.out.println(":::::"+e.getMetadata().getName());
        System.out.println(e.getSpec().getContainers().get(0));
      });
    });
  }

  @Test
  public void tedstFindByLabelEmtxyValue() {
    ConfigBuilder configBuilder = new ConfigBuilder().
        withMasterUrl("http://localhost:8001").
        withNamespace("default");//.withOauthToken(token);
    final Config build = configBuilder.build();
    final DefaultKubernetesClient client = new DefaultKubernetesClient(build);
    assertNotNull(client);
    final ServiceList version = client.services().withLabel("version", "v2").list();
    version.getItems().forEach(s -> {
      System.out.println(s.getMetadata().getName());
    });
  }

  @Test
  public void byName() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");

    client.findServiceByName("spring-boot-crud-admin-step5",services -> {
      System.out.println(services.getMetadata().getName());
    }, errors -> {

    });

  }

  @Test
  public void testClient() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");

    client.findServicesByLabel("name", services -> {
      services.getItems().forEach(s -> {
        System.out.println(s.getMetadata().getName());
      });
    }, errors -> {

    });

    System.out.println("-----------------------");
    client.findServicesByLabel("name","read-ext", services -> {
      services.getItems().forEach(s -> {
        System.out.println(s.getMetadata().getName());
      });
    }, errors -> {

    });

    Map<String, String> h = new HashMap<String, String>() {{
      put("name","read-ext");
      put("version","v2");
    }};

    System.out.println("-----------------------");
    client.findServicesByLabel(h, services -> {
      services.getItems().forEach(s -> {
        System.out.println(s.getMetadata().getName());
      });
    }, errors -> {

    });
  }

  @Test
  public void testMutipleLAbels() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");

    Map<String, String> h = new HashMap<String, String>() {{
      put("name","read-ext2");
      put("version","v2");
    }};

    System.out.println("-----------------------");
    client.findServicesByLabel(h, services -> {
      services.getItems().forEach(s -> {
        System.out.println(s.getMetadata().getName());
      });
    }, errors -> {

    });

    Map<String, String> h2 = new HashMap<String, String>() {{
      put("version","v2");
    }};
    System.out.println("-----------------------");
    client.findServicesByLabel(h2, services -> {
      services.getItems().forEach(s -> {
        System.out.println(s.getMetadata().getName());
      });
    }, errors -> {

    });
  }

  @Test
  public void testLabelsAndName() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");

    Map<String, String> h = new HashMap<String, String>() {{
      put("name","read-ext2");
      put("version","v2");
    }};

    System.out.println("-----------------------");
    client.findServicesByNameAndLabel("read*",h, services -> {
      services.forEach(s -> {
        System.out.println("::"+s.getMetadata().getName());
      });
    }, errors -> {

    });
  }
  @Test
  public void testResolveAnnotationWithServiceName() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");
    TestBean bean = new TestBean();
    client.resolveAnnotations(bean);
    assertNotNull(bean.simpleServiceName);
    System.out.println(bean.simpleServiceName);
  }

  @Test
  public void testResolveAnnotationWithServiceNameAndLabel() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");
    TestBean bean = new TestBean();
    client.resolveAnnotations(bean);
    assertNotNull(bean.simpleServiceNameAndLabel);
    System.out.println(bean.simpleServiceNameAndLabel);
  }

  @Test
  public void testResolveAnnotationWithServiceNameAndManyLabel() {
    final Fabric8DiscoveryClient client = Fabric8DiscoveryClient.builder().
        user(null).
        pwd(null).
        apiToken(null).
        masterUrl("http://localhost:8001").
        namespace("default");
    TestBean bean = new TestBean();
    client.resolveAnnotations(bean);
    assertNotNull(bean.simpleServiceNameAndManyLabel);
    assertTrue(bean.simpleServiceNameAndManyLabel.equals(bean.simpleServiceName));
    System.out.println(bean.simpleServiceNameAndManyLabel);
    System.out.println(bean.simpleServiceNameAndLabel);
  }

  public class TestBean {
    @ServiceName("read-ext2")
    private String simpleServiceName;
    @ServiceName
    @WithLabel(name = "version", value = "v1")
    private String simpleServiceNameAndLabel;
    @ServiceName
    @WithLabels(value = {@WithLabel(name = "version", value = "v2"),@WithLabel(name = "visualize", value = "true")})
    private String simpleServiceNameAndManyLabel;

    public String getSimpleServiceName() {
      return simpleServiceName;
    }



    public String getSimpleServiceNameAndLabel() {
      return simpleServiceNameAndLabel;
    }



    public String getSimpleServiceNameAndManyLabel() {
      return simpleServiceNameAndManyLabel;
    }


  }

}
