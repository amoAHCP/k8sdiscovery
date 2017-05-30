package org.jacpfx.weld;

/**
 * Created by amo on 17.05.17.
 */
import javax.enterprise.event.*;
import org.jacpfx.weld.event.SimpleEvent;


public class App {
  public void onEvent(@Observes SimpleEvent ignored, SimpleService service) {
    service.greet();
  }
}