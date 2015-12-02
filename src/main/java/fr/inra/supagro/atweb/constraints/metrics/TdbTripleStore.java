package fr.inra.supagro.atweb.constraints.metrics;


import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileUtils;

import java.io.*;

public class TdbTripleStore implements TripleStore {
    private Model model;
    private Dataset dataset;

    public void open(String pathToTtlFile) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File("tdb"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataset = TDBFactory.createDataset("tdb");
        model = dataset.getDefaultModel();

        System.out.printf("TDB model size initially: %d\n", model.size());
        System.out.printf("Loading file %s\n", pathToTtlFile);

        // If model is empty, populate it with annotations and biorefinery ontology triples
        if(model.size() == 0) {
            InputStream ttlFileStream = null;
            try {
                ttlFileStream = new FileInputStream(pathToTtlFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            model.read(ttlFileStream, null, FileUtils.langTurtle);
//            model.read(App.class.getResourceAsStream("/IC2ACV.owl"), null);
        }
    }

    public void close() {
        dataset.close();
    }

    public QueryExecution executeQuery(String q, Integer docId) {
        return QueryExecutionFactory.create(q, model);
    }

    public Model getModel() {
        return model;
    }
}