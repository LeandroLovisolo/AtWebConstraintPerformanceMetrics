package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

public interface TripleStore {
    void open(String pathToTtlFile);
    void close();
    QueryExecution executeQuery(String q, Integer docId);
    Model getModel();
}