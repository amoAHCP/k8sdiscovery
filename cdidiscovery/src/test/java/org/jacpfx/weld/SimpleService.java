package org.jacpfx.weld;

/**
 * Created by amo on 17.05.17.
 */
import io.fabric8.annotations.ServiceName;
import javax.inject.*;

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