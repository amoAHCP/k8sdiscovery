package org.jacpfx.weld;


import org.jacpfx.weld.event.SimpleEvent;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Created by amo on 17.05.17.
 */
public class CDI2Demo {
  public static void main(String... args) {
   /** SeContainerInitializer containerInit = SeContainerInitializer.newInstance();
    SeContainer container = containerInit.initialize();


    container.getBeanManager().fireEvent(new SimpleEvent());

    container.close();
**/

    Weld weld = new Weld();
    WeldContainer container = weld.initialize();
    container.getBeanManager().fireEvent(new SimpleEvent());
    weld.shutdown();
  }
}
