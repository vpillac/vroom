package vroom.trsp.sandbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.visualization.DefaultInstanceGraph;
import vroom.common.modeling.visualization.DefaultInstanceViewer;
import vroom.common.modeling.visualization.VisualizationFrame;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.legacy.TRSPLegacyInstance;

public class TRSPSolomonVisualization {

    public static String INSTANCE = "../Instances/trsp/pillac/RC202_5-5-5.txt";

    public static HashSet<IVRPRequest> printRequestList(TRSPLegacyInstance instance) {
        HashSet<IVRPRequest> leftover = new HashSet<IVRPRequest>(instance.getRequests());

        List<IVRPRequest> requests = instance.getRequests();
        Collections.sort(requests, new ObjectWithIdComparator());

        for (Vehicle v : instance.getFleet()) {
            Technician t = (Technician) v;
            System.out.printf("\n%s\n\tDirectly Compatible Requests:\n", t);

            for (IVRPRequest r : requests) {
                TRSPRequest req = (TRSPRequest) r;
                if (req.getSkillSet().isCompatibleWith(t.getSkillSet())
                        && req.getToolSet().isCompatibleWith(t.getToolSet())) {
                    System.out.printf("\t%s\n", req);
                    leftover.remove(req);
                }
            }

            System.out.printf("\n\t- Compatible Requests:\n", t);

            for (IVRPRequest r : requests) {
                TRSPRequest req = (TRSPRequest) r;
                if (req.getSkillSet().isCompatibleWith(t.getSkillSet())
                        && !req.getToolSet().isCompatibleWith(t.getToolSet())) {
                    System.out.printf("\t- %s\n", req);
                }
            }
        }

        LinkedList<IVRPRequest> lo = new LinkedList<IVRPRequest>(leftover);
        Collections.sort(lo, new ObjectWithIdComparator());
        System.out.printf("\n Leftovers: %s/%s\n", lo.size(), requests.size());
        for (IVRPRequest req : lo) {
            System.out.printf("\t%s\n", req);
        }

        return leftover;

    }

    public static VisualizationFrame showInstance(TRSPLegacyInstance instance, Transformer<INodeVisit, Paint> trans) {
        DefaultInstanceGraph graph = new DefaultInstanceGraph(instance);
        DefaultInstanceViewer view = new DefaultInstanceViewer(graph);
        view.getRenderContext().setVertexFillPaintTransformer(trans);

        VisualizationFrame frame = new VisualizationFrame(instance.getName(), view);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logging.awaitLogging(60000);
                System.exit(0);
            }
        });
        frame.setPreferredSize(new Dimension(400, 400));
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public static class VertexFillPaintTransformer implements Transformer<INodeVisit, Paint> {

        Paint[] mPaints;

        public VertexFillPaintTransformer(int skillCount) {
            mPaints = new Paint[skillCount];

            Random r = new Random();

            for (int i = 0; i < mPaints.length; i++) {
                mPaints[i] = new Color(r.nextInt());
            }
        }

        @Override
        public Paint transform(INodeVisit node) {
            if (node.isDepot())
                return Color.BLACK;
            else

                return mPaints[((TRSPRequest) node.getParentRequest()).getSkillSet().toList().get(0)];
        }

    }

    public static class CompatibleVertex implements Transformer<INodeVisit, Paint> {
        private final Technician           technician;
        private final Set<TRSPRequest>     leftovers;
        private final HashSet<TRSPRequest> exclusive;

        public CompatibleVertex(Technician technician, TRSPInstance instance) {
            super();
            this.technician = technician;
            this.leftovers = new HashSet<TRSPRequest>(instance.getRequests());
            this.exclusive = new HashSet<TRSPRequest>();

            for (TRSPRequest req : instance.getRequests()) {
                if (req.getSkillSet().isCompatibleWith(technician.getSkillSet())
                        && req.getToolSet().isCompatibleWith(technician.getToolSet())) {
                    exclusive.add(req);
                    leftovers.remove(req);
                }
                for (Vehicle v : instance.getFleet()) {
                    Technician t = (Technician) v;
                    if (t != technician && req.getSkillSet().isCompatibleWith(t.getSkillSet())
                            && req.getToolSet().isCompatibleWith(t.getToolSet())) {
                        leftovers.remove(req);
                        exclusive.remove(req);
                    }
                }
            }
        }

        @Override
        public Paint transform(INodeVisit node) {
            if (node.getNode() == technician.getHome())
                return Color.ORANGE;
            else if (node.isDepot() && node.getID() > 0)
                return Color.LIGHT_GRAY;
            else if (node.isDepot())
                return Color.BLACK;
            else {
                int comp = getCompatibility((TRSPRequest) node.getParentRequest(), technician);
                if (comp == 7)
                    return Color.GREEN;
                else if (comp == 6)
                    return Color.BLUE;
                else if (comp == 5)
                    return Color.CYAN;
                else
                    return Color.GRAY;
            }
        }

    }

    /**
     * @param request
     * @param technician
     * @return <code>4*skillComp + 2*toolComp + 1*spareComp</code>
     */
    public static int getCompatibility(TRSPRequest request, Technician technician) {
        int result = 0;
        if (request.getSkillSet().isCompatibleWith(technician.getSkillSet()))
            result = 4;
        boolean spareComp = true;
        for (int s = 0; s < technician.getCompartmentCount(); s++)
            if (request.getSparePartRequirement(s) > technician.getAvailableSpareParts(s)) {
                spareComp = false;
                break;
            }
        result += request.getToolSet().isCompatibleWith(technician.getToolSet()) ? 2 : 0;
        result += spareComp ? 1 : 0;
        return result;
    }

    public static class CompatibleTechniciansVertex implements Transformer<INodeVisit, Paint> {

        HashMap<INodeVisit, Color> paints;

        public CompatibleTechniciansVertex(TRSPLegacyInstance instance) {
            paints = new HashMap<INodeVisit, Color>(instance.getRequestCount());

            float numTec = instance.getFleet().size() / 5;
            for (INodeVisit n : instance.getNodeVisits()) {
                int count = 0;
                for (Vehicle v : instance.getFleet()) {
                    Technician t = (Technician) v;
                    TRSPRequest r = (TRSPRequest) n.getParentRequest();
                    if (t.getSkillSet().isCompatibleWith(r.getSkillSet())
                            && t.getToolSet().isCompatibleWith(r.getToolSet()))
                        count++;
                }
                if (count == 0)
                    paints.put(n, new Color(1f, 0, 0));
                else {
                    float fill = Math.min(1f, count / numTec);
                    paints.put(n, new Color((1 - fill) / 2, fill, 0));
                }
            }
        }

        @Override
        public Paint transform(INodeVisit node) {
            if (node.isDepot())
                return Color.BLACK;
            return paints.get(node);
        }

    }
}
