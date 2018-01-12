package eu.arrowhead.common.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import eu.arrowhead.common.database.ArrowheadSystem;

public class ArrowheadSystemKeyDeserializer extends KeyDeserializer {

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) {
    return new ArrowheadSystem(key);
  }
}
