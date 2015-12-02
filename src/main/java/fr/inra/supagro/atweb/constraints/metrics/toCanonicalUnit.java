package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class toCanonicalUnit extends FunctionBase2 {
    public toCanonicalUnit() { super() ; }
    public NodeValue exec(NodeValue qty, NodeValue unit) {
        System.out.println("toCanonicalUnit function called.");
        return NodeValue.makeNodeFloat(qty.getFloat());
    }
}