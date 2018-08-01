package eu.arrowhead.service_registry;

import eu.arrowhead.common.dto.entity.ArrowheadService;
import eu.arrowhead.common.dto.entity.ArrowheadSystem;
import eu.arrowhead.common.dto.entity.ServiceRegistryEntry;
import eu.arrowhead.service_registry.api.repository.ServiceRegistryRepo;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ServiceRegistryApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integration-test.properties")
public class ServiceRegistryApplicationTests {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private static ServiceRegistryRepo repository;

  @BeforeClass
  public static void saveServiceRegEntry() {
    //Create the ArrowheadService
    Map<String, String> metadata = new HashMap<>();
    metadata.put("key", "value");
    Set<String> interfaces = new HashSet<>();
    interfaces.add("interface1");
    interfaces.add("interface2");
    ArrowheadService service = new ArrowheadService("testService", interfaces, metadata);

    //Create the ArrowheadSystem
    ArrowheadSystem provider = new ArrowheadSystem("testSystem", "localhost", 80, null);

    //Create the ServiceRegEntry
    ServiceRegistryEntry entry = new ServiceRegistryEntry(service, provider, "api/test", false, LocalDateTime.now().plusDays(1));
    repository.save(entry);
  }

  @Test
  public void contextLoads() {
  }

  @Test
  public void testGetByExample() {
    Set<String> interfaces = new HashSet<>();
    interfaces.add("interface1");
    interfaces.add("interface3");
    ArrowheadService service = new ArrowheadService("testService", interfaces, null);

    ArrowheadSystem provider = new ArrowheadSystem("testSystem", "localhost", 80, null);

    ServiceRegistryEntry entry = new ServiceRegistryEntry();
    entry.setService(service);
    entry.setProvider(provider);

    Optional<ServiceRegistryEntry> retrievedEntry = repository.findByServiceAndProvider(entry.getService(), entry.getProvider());
    System.out.println(retrievedEntry.toString());
  }
}
