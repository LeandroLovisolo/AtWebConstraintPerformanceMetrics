package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

public class PrePopulatedTdbTripleStore implements TripleStore {
    private Model model;
    private Dataset dataset;

    public void open(String pathToTdbStore) {

        dataset = TDBFactory.createDataset(pathToTdbStore);
        model = dataset.getDefaultModel();
    }

    public void close() {
        dataset.close();
    }

    public QueryExecution executeQuery(String q, Integer docId) {
        String graphName = "Document_" + docId;
        Model namedModel = dataset.getNamedModel(graphName);
        System.out.printf("\nNamed graph: %s. Size: %d\n", graphName, namedModel.size());
        return QueryExecutionFactory.create(q, namedModel);
    }

    public Model getModel() {
        return model;
    }
}
