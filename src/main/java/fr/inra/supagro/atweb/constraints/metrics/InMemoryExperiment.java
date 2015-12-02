package fr.inra.supagro.atweb.constraints.metrics;


import org.apache.commons.cli.Options;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.List;

public class InMemoryExperiment extends Experiment {
    private Model model;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();
        options.addOption("triples", true, "Path to triples file (Turtle format)");
        return options;
    }

    @Override
    protected List<Stats> doRun(String pathToQueries) {
        model = ModelFactory.createDefaultModel();
        readTtl(model, commandLine.getOptionValue("triples"));
        return super.doRun(pathToQueries);
    }

    @Override
    protected QueryExecution executeQuery(String query, Integer docId) {
        return QueryExecutionFactory.create(query, model);
    }

    public static void main(String args[]) {
        new InMemoryExperiment().run(args);
    }
}
