package eu.arrowhead.common.json;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;

public class MarshallingFeature implements Feature {

  private final static String JSON_FEATURE = MarshallingFeature.class.getSimpleName();

  @Override
  public boolean configure(FeatureContext context) {
    context.register(JacksonJsonProviderAtRest.class, MessageBodyReader.class, MessageBodyWriter.class);
    final Configuration config = context.getConfiguration();

    // Disables discoverability of org.glassfish.jersey.jackson.JacksonFeature
    context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()), JSON_FEATURE);
    return true;
  }

}
