package fr.inra.supagro.atweb.constraints.metrics;


import org.apache.commons.cli.*;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class Experiment {

    public static final String QUERIES_ROOT = "queries";

    protected CommandLine commandLine;

    protected String getPathToQueries() {
        return commandLine.getOptionValue("queries");
    }

    protected List<Integer> getDocIds() {
        return Arrays.asList(1271, 1272, 1273);
    }

    protected abstract QueryExecution executeQuery(String query, Integer docId);

    protected void readTtl(Model model, String path) {
        System.out.printf("Loading file %s...\n", path);
        InputStream ttlFileStream = null;
        try {
            ttlFileStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        model.read(ttlFileStream, null, FileUtils.langTurtle);
        System.out.printf("Model size after loading file: %d triples.\n", model.size());
    }

    protected Options getOptions() {
        Options options = new Options();
        options.addOption("queries", true, "Path to directory where to look for SPARQL queries");
        options.addOption("csv", true, "Path to file in which to store statistics (CSV format)");
        options.addOption("help", "Print help");
        return options;
    }

    private CommandLine cli(String args[]) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
        }
        return line;
    }

    private void printStats(List<Stats> stats, PrintStream out) {
        out.println("queryPath, docId, milliseconds");
        for(Stats s : stats) {
            out.println(s.queryPath + ", " + s.docId.toString() + ", " + s.milliseconds.toString());
        }
    }

    private static List<AtWebQuery> getQueries(String pathToQueries) {
        List<AtWebQuery> queries = new ArrayList<AtWebQuery>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                org.apache.commons.io.FileUtils.getFile(pathToQueries),
                new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        for(File file : files) {
            try {
                String path = file.getPath();
                String query = new String(Files.readAllBytes(Paths.get(path)), "utf-8");
                queries.add(new AtWebQuery(path, query));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return queries;
    }

    protected static class ExecutionResults {
        public long runTime;
        public List<QuerySolution> solutions = new ArrayList<QuerySolution>();
    }

    protected ExecutionResults executeAndMeasure(QueryExecution qe) {
        ExecutionResults executionResults = new ExecutionResults();
        long t0 = System.currentTimeMillis();
        ResultSet results = qe.execSelect();
        while(results.hasNext()) {
            QuerySolution soln = results.nextSolution();
            executionResults.solutions.add(soln);
        }
        executionResults.runTime = System.currentTimeMillis() - t0;
        return executionResults;
    }

    protected List<Stats> doRun(String pathToQueries) {
        List<Stats> stats = new ArrayList<Stats>();
        List<AtWebQuery> queries = getQueries(pathToQueries);
        List<Integer> docIds = getDocIds();

        for(AtWebQuery query : queries) {
            System.out.println("\nExecuting query " + query.path + "...");

            boolean first = true;

            for(Integer docId : docIds) {
                if(first) first = false;
                else System.out.println("");

                System.out.printf("  Querying docId %d... ", docId);

                AtWebQuery boundQuery = query.bindDocId(docId);
                QueryExecution qexec = executeQuery(boundQuery.query, docId);

                ExecutionResults executionResults = executeAndMeasure(qexec);
                stats.add(new Stats(query.path, docId, executionResults.runTime));

                System.out.printf("took %d milliseconds.\n", executionResults.runTime);

                if(executionResults.solutions.isEmpty()) {
                    System.out.println("  Query returned an empty set of solutions.");
                } else {
                    System.out.println("  Solutions returned by the query:");
                    for (QuerySolution soln : executionResults.solutions) {
                        System.out.println(soln.toString());
                    }
                }
            }
        }

        return stats;
    }

    public void run(String args[]) {
        commandLine = cli(args);

        if(commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("experiment", getOptions());
            return;
        }

        PrintStream csv = null;
        String csvOpt = commandLine.getOptionValue("csv");
        if(csvOpt != null) try {
            csv = new PrintStream(csvOpt, "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        List<Stats> stats = doRun(getPathToQueries());

        System.out.println("\n--------------------------------------------------\n");
        System.out.println("Stats summary:");
        printStats(stats, System.out);

        if(csv != null) printStats(stats, csv);
    }

}
