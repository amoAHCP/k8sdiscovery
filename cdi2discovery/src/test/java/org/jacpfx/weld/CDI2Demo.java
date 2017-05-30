package org.jacpfx.weld;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import org.jacpfx.weld.event.SimpleEvent;

/**
 * Created by amo on 17.05.17.
 */
public class CDI2Demo {
  public static void main(String... args) {
    SeContainerInitializer containerInit = SeContainerInitializer.newInstance();
    SeContainer container = containerInit.initialize();

    // Fire synchronous event that triggers the code in App class.
    container.getBeanManager().fireEvent(new SimpleEvent());

    container.close();
  }
}
