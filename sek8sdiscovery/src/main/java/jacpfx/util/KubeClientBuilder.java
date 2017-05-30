package jacpfx.util;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by amo on 06.04.17.
 */
public class KubeClientBuilder {

  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";

  public static KubernetesClient buildKubernetesClient(String apiToken, String kubernetesMaster) {
    String oauthToken = apiToken;
    if (StringUtil.isNullOrEmpty(oauthToken)) {
      oauthToken = getAccountToken();
    }
    if (StringUtil.isNullOrEmpty(oauthToken)) {
      return new DefaultKubernetesClient();
    }
    final Config config = new ConfigBuilder().withOauthToken(oauthToken).withMasterUrl(kubernetesMaster)
        .build();
    return new DefaultKubernetesClient(config);
  }


  private static String getAccountToken() {
    try {
      final Path path = Paths.get(IO_SERVICEACCOUNT_TOKEN);
      if (!path.toFile().exists()) {
        return null;
      }
      return new String(Files.readAllBytes(path));

    } catch (IOException e) {
      throw new RuntimeException("Could not get token file", e);
    }
  }

}
