package vroom.optimization.pl.gurobi;

import java.util.HashSet;
import java.util.Set;

import vroom.common.utilities.graphs.Cut;

public class ViolatedSubtour implements Comparable<ViolatedSubtour> {
    private final double violation;
    private final Cut    cut;
    private final double rhs;

    public double getViolation() {
        return violation;
    }

    public Cut getCut() {
        return cut;
    }

    public double getRhs() {
        return rhs;
    }

    protected ViolatedSubtour(double violation, Cut cut, double rhs) {
        super();
        this.violation = violation;
        this.cut = cut;
        this.rhs = rhs;
    }

    protected ViolatedSubtour(double violation, Set<Integer> cut, double rhs, int nodeCount) {
        super();
        this.violation = violation;
        Set<Integer> comp = new HashSet<Integer>();
        for (int i = 0; i < nodeCount; i++) {
            if (!cut.contains(i)) {
                comp.add(i);
            }
        }

        this.cut = new Cut(cut, comp);
        this.rhs = rhs;
    }

    @Override
    public int compareTo(ViolatedSubtour o) {
        return Double.compare(getViolation(), o.getViolation());
    }

    @Override
    public String toString() {
        return String.format("subtour:%s rhs:%s violation:%s", getCut().getCut(), getRhs(), getViolation());
    }
}