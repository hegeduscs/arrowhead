package eu.arrowhead.common.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonJsonProviderAtRest extends JacksonJaxbJsonProvider {

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.ALWAYS, Include.NON_NULL));
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
  }

  public JacksonJsonProviderAtRest() {
    super();
    setMapper(mapper);
  }

  public static ObjectMapper getMapper() {
    return mapper;
  }

}
