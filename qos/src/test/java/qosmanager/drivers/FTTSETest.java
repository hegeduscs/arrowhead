/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
*/
package qosmanager.drivers;

import static org.junit.Assert.assertEquals;

import eu.arrowhead.qos.communication.drivers.FTTSE;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Paulo
 */
public class FTTSETest {

  public FTTSETest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of testCalculateSize method, of class FTTSE.
   */
  @Test
  public void testCalculateSize() {
    System.out.println("calculateSize");
    Integer mtu = 1500;
    FTTSE instance = new FTTSE();
    int expResult = 7500;
    int result = instance.calculateSize(mtu);
    assertEquals(expResult, result);

  }

  /**
   * Test of generateCommands method, of class FTTSE. With QoS, bandwitdh 1500B/s, delay 20 ms.
   */
  @Test
  public void testGenerateCommands1() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();
    requestedQoS.put("bandwidht", "1500");
    requestedQoS.put("delay", "20");

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "1");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "0");

    Map<String, String> result = instance.
        generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
    assertEquals(expResult, result);
  }

  /**
   * Test of generateCommands method, of class FTTSE. No QoS.
   */
  @Test
  public void testGenerateCommands2() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "5");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "3");

    Map<String, String> result = instance.
        generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
    assertEquals(expResult, result);
  }

  /**
   * Test of generateCommands method, of class FTTSE. With invalid QoS.
   */
  @Test
  public void testGenerateCommands3() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();
    requestedQoS.put("ERROR", "1500");
    requestedQoS.put("NULL", "20");

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "5");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "0");

    Map<String, String> result = instance.
        generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
    assertEquals(expResult, result);
  }

  /**
   * Test of generateCommands method, of class FTTSE. With QoS, bandwitdh 1500B/s, delay 20 ms.
   */
  @Test
  public void testGenerateCommands4() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();
    requestedQoS.put("bandwidht", "");
    requestedQoS.put("delay", "");

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "1");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "0");

    try {
      Map<String, String> result = instance.
          generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
      assertEquals(expResult, result);
    } catch (NumberFormatException e) {
      assertEquals(1, 1);
    }
  }

  /**
   * Test of generateCommands method, of class FTTSE. With QoS, invalid bandwitdh , delay 20 ms.
   */
  @Test
  public void testGenerateCommands5() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();
    requestedQoS.put("bandwidht", "X");
    requestedQoS.put("delay", "10");

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "1");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "0");

    try {
      Map<String, String> result = instance.
          generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
      assertEquals(expResult, result);
    } catch (IllegalArgumentException e) {
      assertEquals(1, 1);
    }

  }

  /**
   * Test of generateCommands method, of class FTTSE. With QoS, invalid bandwitdh , delay 20 ms.
   */
  @Test
  public void testGenerateCommands6() {
    System.out.println("generateCommands");
    Integer streamID = 1;
    Integer elementaryCycle = 20;
    Integer mtu = 1500;
    Map<String, String> requestedQoS = new HashMap<>();
    requestedQoS.put("bandwidht", "1500");
    requestedQoS.put("delay", "X");

    FTTSE instance = new FTTSE();
    Map<String, String> expResult = new HashMap<>();
    expResult.put("PERIOD", "1");
    expResult.put("SIZE", "7500");
    expResult.put("ID", "1");
    expResult.put("SYNCHRONOUS", "0");

    try {
      Map<String, String> result = instance.
          generateCommands(streamID, elementaryCycle, mtu, requestedQoS);
      assertEquals(expResult, result);
    } catch (NumberFormatException e) {
      assertEquals(1, 1);
    }
  }
}
