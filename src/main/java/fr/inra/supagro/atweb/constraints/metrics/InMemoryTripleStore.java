package fr.inra.supagro.atweb.constraints.metrics;


import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class InMemoryTripleStore implements TripleStore {
    private Model model;

    public void open(String pathToTtlFile) {
        model = ModelFactory.createDefaultModel();

        InputStream ttlFileStream = null;
        try {
            ttlFileStream = new FileInputStream(pathToTtlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        model.read(ttlFileStream, null, FileUtils.langTurtle);
        model.read(App.class.getResourceAsStream("/IC2ACV.owl"), null);
    }

    public void close() {
    }

    public QueryExecution executeQuery(String q, Integer docId) {
        return QueryExecutionFactory.create(q, model);
    }

    public Model getModel() {
        return model;
    }
}
