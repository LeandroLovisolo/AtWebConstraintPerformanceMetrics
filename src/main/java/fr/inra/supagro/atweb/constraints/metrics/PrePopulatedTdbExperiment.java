package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.commons.cli.Options;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.util.List;

public class PrePopulatedTdbExperiment extends Experiment {
    private Dataset dataset;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();
        options.addOption("tdbstore", true, "Path to TDB store");
        return options;
    }

    @Override
    protected List<Stats> doRun(String pathToQueries) {
        dataset = TDBFactory.createDataset(commandLine.getOptionValue("tdbstore"));
        List<Stats> stats =  super.doRun(pathToQueries);
        dataset.close();
        return stats;
    }

    @Override
    protected QueryExecution executeQuery(String query, Integer docId) {
        String graphName = "Document_" + docId;
        Model namedModel = dataset.getNamedModel(graphName);
        System.out.printf("  Querying against named graph: %s. Size: %d triples.\n", graphName, namedModel.size());
        return QueryExecutionFactory.create(query, namedModel);
    }

    public static void main(String[] args) {
        new PrePopulatedTdbExperiment().run(args);
    }
}
