package org.jacpfx.discovery.extension;

import io.fabric8.annotations.ServiceName;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import org.jacpfx.discovery.annotation.K8SDiscovery;
import org.jacpfx.discovery.annotation.Label;
import org.jacpfx.util.KubeClientBuilder;
import org.jacpfx.util.ServiceUtil;

/**
 * Created by amo on 18.05.17.
 */
public class K8SExtension implements Extension {

  private Logger log = Logger.getLogger(K8SExtension.class.getName());

  public <T> void initializePropertyLoading(final @Observes ProcessInjectionTarget<T> pit) {
    final AnnotatedType<T> at = pit.getAnnotatedType();
    if (!at.isAnnotationPresent(K8SDiscovery.class)) {
      return;
    }
    final K8SDiscovery discovery = at.getAnnotation(K8SDiscovery.class);
    final String user = discovery.user();
    final String pwd = discovery.pwd();
    final String api_token = discovery.api_token();
    final String master_url = discovery.master_url();
    final String namespace = discovery.namespace();
    final KubernetesClient kubernetesClient = KubeClientBuilder
        .buildKubernetesClient(user, pwd, api_token, master_url, namespace);
    final InjectionTarget<T> it = pit.getInjectionTarget();
    final InjectionTarget<T> wrapped = new InjectionTarget<T>() {
      @Override
      public void inject(T instance, CreationalContext<T> ctx) {
        it.inject(instance, ctx);
        final Set<AnnotatedField<? super T>> fields = at.getFields();
        final List<Field> serviceNameFileds = fields.stream()
            .filter(f -> f.isAnnotationPresent(ServiceName.class)).map(f -> f.getJavaMember())
            .collect(Collectors.toList());
        final List<Field> labelFields = fields.stream()
            .filter(f -> f.isAnnotationPresent(Label.class)).map(f -> f.getJavaMember())
            .collect(Collectors.toList());
        try {
          log.info("namespace : " + namespace);
          ServiceUtil.findServiceEntryAndSetValue(instance, serviceNameFileds, kubernetesClient);
          ServiceUtil.findLabelAndSetValue(instance, labelFields, kubernetesClient);
        } catch (Exception e) {
          log.info("no client connection: "+e.getMessage());
        }


      }


      @Override
      public void postConstruct(T instance) {
        it.postConstruct(instance);
      }


      @Override
      public void preDestroy(T instance) {
        it.dispose(instance);
      }


      @Override
      public void dispose(T instance) {
        it.dispose(instance);
      }


      @Override
      public Set<InjectionPoint> getInjectionPoints() {
        return it.getInjectionPoints();
      }


      @Override
      public T produce(CreationalContext<T> ctx) {
        return it.produce(ctx);
      }
    };
    pit.setInjectionTarget(wrapped);
  }


}
