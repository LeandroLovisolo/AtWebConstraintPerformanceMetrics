package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.commons.cli.Options;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TdbExperiment extends Experiment {
    private Dataset dataset;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();
        options.addOption("triples", true, "Path to triples file (Turtle format)");
        return options;
    }

    @Override
    protected List<Stats> doRun(String pathToQueries) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File("tdb"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataset = TDBFactory.createDataset("tdb");
        Model model = dataset.getDefaultModel();
        readTtl(model, commandLine.getOptionValue("triples"));

        List<Stats> stats = super.doRun(pathToQueries);

        dataset.close();
        return stats;
    }

    @Override
    protected QueryExecution executeQuery(String query, Integer docId) {
        return QueryExecutionFactory.create(query, dataset.getDefaultModel());
    }

    public static void main(String args[]) {
        new TdbExperiment().run(args);
    }

}
