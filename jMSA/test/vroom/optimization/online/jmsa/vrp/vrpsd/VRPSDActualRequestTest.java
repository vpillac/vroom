/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import umontreal.iro.lecuyer.probdist.NormalDist;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.StochasticDemand;

/**
 * <code>VRPSDActualRequestTest</code> is a test case for
 * {@link VRPSDActualRequest}
 * <p>
 * Creation date: May 5, 2010 - 10:37:37 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDActualRequestTest {

	VRPSDActualRequest vrpReq;

	double mu = 6, sigma = 2, dem = 8;

	@Before
	public void setUp() throws Exception {
		Request req = new Request(0, new Node(0, new PointLocation(0, 0)));
		req.setAttribute(RequestAttributeKey.DEMAND, new StochasticDemand(
				new NormalDist(mu, sigma)));

		vrpReq = new VRPSDActualRequest(NodeVisit.createNodeVisits(req)[0]);

		System.out.println("Generated request:");
		System.out.println(vrpReq);
	}

	/**
	 * Test method for
	 * {@link vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest#setActualDemands(double[])}
	 * .
	 */
	@Test
	public void testSetActualDemand() {
		System.out.println("-------------------------------");
		System.out.println(" testSetActualDemand");
		System.out.println("-------------------------------");
		try {
			vrpReq.setActualDemands(dem);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			vrpReq.setActualDemands(dem, 1);
			fail("Setting more demands than the number of product should raise an exception");
		} catch (IllegalArgumentException ia) {
			// Ok
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest#getDemand(int)}
	 * .
	 */
	@Test
	public void testGetDemandInt() {
		System.out.println("-------------------------------");
		System.out.println(" testGetDemandInt");
		System.out.println("-------------------------------");

		assertEquals(mu, vrpReq.getDemand(), 0.00001);

		assertEquals(mu, vrpReq.getDemand(0), 0.00001);

		System.out.println("Setting the actual demand to " + dem);
		vrpReq.setActualDemands(dem);
		System.out.println("Resulting request: \n" + vrpReq);

		assertEquals(dem, vrpReq.getDemand(), 0.00001);
		assertEquals(dem, vrpReq.getDemand(0), 0.00001);

	}

	/**
	 * Test method for
	 * {@link vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDActualRequest#isDemandKnown(int)}
	 * .
	 */
	@Test
	public void testIsDemandKnown() {
		System.out.println("-------------------------------");
		System.out.println(" testIsDemandKnown");
		System.out.println("-------------------------------");
		assertFalse(vrpReq.isDemandKnown(0));

		System.out.println("Setting the actual demand to " + dem);
		vrpReq.setActualDemands(dem);
		System.out.println("Resulting request: \n" + vrpReq);

		assertTrue(vrpReq.isDemandKnown(0));
	}

}
