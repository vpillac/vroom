package vroom.optimization.online.jmsa.vrp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.util.VRPInstanceBuilder;
import vroom.optimization.online.jmsa.MSAGlobalParameters;

/**
 * <code>VRPScenarioTest</code> is a test case for the {@link VRPScenario} class Creation date: May 3, 2010 - 12:33:25 PM<br/>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPScenarioTest {

    VRPScenario            scen;
    List<VRPActualRequest> actualRequests;

    @Before
    public void setUp() throws Exception {
        MSAVRPInstance vrpInstance = new MSAVRPInstance(VRPInstanceBuilder.newSimpleDynamicInstance(10, 100,
            10, 10, 0, false), new MSAGlobalParameters());

        actualRequests = new LinkedList<VRPActualRequest>();
        List<VRPSampledRequest> sampledRequests = new LinkedList<VRPSampledRequest>();

        for (INodeVisit n : vrpInstance.getNodeVisits()) {
            actualRequests.add(new VRPActualRequest(n));
        }

        scen = new VRPScenario(vrpInstance, actualRequests, sampledRequests);

        scen.acquireLock();

        scen.addRoute(new VRPScenarioRoute(scen, vrpInstance.getFleet().getVehicle()));

        for (VRPActualRequest r : actualRequests) {
            scen.getRoute(0).appendNode(r);
        }

        scen.releaseLock();

    }

    @Test
    public void testCloneObject() {
        System.out.println("----------------------");
        System.out.println("  testCloneObject ");
        System.out.println("----------------------");

        scen.acquireLock();
        VRPScenario clone = scen.clone();
        clone.acquireLock();

        System.out.println("Scenario:");
        System.out.println(scen);
        System.out.println("Clone:");
        System.out.println(clone);

        assertTrue(clone != scen);

        assertTrue("Shared reference", clone.getLockInstance() != scen.getLockInstance());

        assertEquals("Original and clone should have the same number of routes", clone.getRouteCount(),
            scen.getRouteCount());

        for (int r = 0; r < clone.getRouteCount(); r++) {

            assertTrue("Shared reference", clone.getRoute(r) != scen.getRoute(r));

            assertEquals("Original and clone route should have the same length", clone.getRoute(r).length(),
                scen.getRoute(r).length());

            assertEquals("Original and clone route should have the same cost", clone.getRoute(r).getCost(),
                scen.getRoute(r).getCost(), 0.0001);

            assertTrue("Cloned route should have a reference to the cloned scenario", clone.getRoute(r)
                .getParentSolution() == clone);

            for (int p = 0; p < clone.getRoute(r).getVehicle().getCompartmentCount(); p++) {
                assertEquals("Original and clone route should have the same load",
                    clone.getRoute(r).getLoad(p), scen.getRoute(r).getLoad(p), 0.0001);
            }

            for (int n = 0; n < clone.getRoute(r).length(); n++) {
                assertTrue("Unshared reference", clone.getRoute(r).getNodeAt(n) == scen.getRoute(r)
                    .getNodeAt(n));
            }
        }

        scen.releaseLock();
        clone.releaseLock();
    }

}
