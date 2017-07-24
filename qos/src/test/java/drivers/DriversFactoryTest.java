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
package drivers;

import eu.arrowhead.qos.communication.drivers.FTTSE;
import eu.arrowhead.qos.drivers.DriversFactory;
import eu.arrowhead.qos.drivers.ReservationInfo;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Paulo
 */
public class DriversFactoryTest {

	public DriversFactoryTest() {
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
	 * Test of getInstance method, of class DriversFactory.
	 */
	@Test
	public void testGetInstance() {
		System.out.println("getInstance");
		DriversFactory result = DriversFactory.getInstance();
		if (result != null) {
			assertEquals(1, 1);
		} else {
			assertEquals(1, 0);
		}
	}

	/**
	 * Test of findClass method, of class DriversFactory.
	 */
	@Test
	public void testFindClass() throws Exception {
		System.out.println("findClass");
		String communicationProtocol = "FTTSE";
		DriversFactory instance = DriversFactory.getInstance();
		FTTSE expResult = new FTTSE();
		Class result = instance.findClass(communicationProtocol);
		assertEquals(expResult.getClass(), result);

	}

	/**
	 * Test of findClass method, of class DriversFactory.
	 */
	@Test
	public void testFindClass1() throws Exception {
		System.out.println("findClass");
		String communicationProtocol = "FTT-SE";
		DriversFactory instance = DriversFactory.getInstance();
		FTTSE expResult = new FTTSE();
		try {
			Class result = instance.findClass(communicationProtocol);
		} catch (ClassNotFoundException e) {
			assertEquals(1, 1);
			return;
		}
		fail("Class Not Found Error !");
	}

	/**
	 * Test of findMethod method, of class DriversFactory.
	 */
	@Test
	public void testFindMethod() throws Exception {
		System.out.println("findMethod");
		String communicationProtocol = "FTTSE";

		DriversFactory instance = DriversFactory.getInstance();
		Method expResult = FTTSE.class.
			getDeclaredMethod("reserveQoS", ReservationInfo.class);
		Class cls = instance.findClass(communicationProtocol);
		Method result = instance.findMethod(cls);
		assertEquals(expResult, result);

	}

}
