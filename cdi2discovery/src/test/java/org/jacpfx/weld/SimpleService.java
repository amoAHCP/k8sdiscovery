package org.jacpfx.weld;

/**
 * Created by amo on 17.05.17.
 */
import io.fabric8.annotations.ServiceName;
import javax.inject.*;
import org.jacpfx.discovery.annotation.K8SDiscovery;

@K8SDiscovery
public class SimpleService {
  @Inject
  private Hello greeter;

  @ServiceName("test")
  private String test;

  public void greet()
  {
    greeter.greet();
  }
}