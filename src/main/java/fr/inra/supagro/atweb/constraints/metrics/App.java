package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.commons.cli.*;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.jena.query.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class App
{

    private static TripleStore tripleStore;

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

    private static List<Integer> getDocIds() {
        List<Integer> docIds = new ArrayList<Integer>();

//        String q = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                   "prefix anno: <http://opendata.inra.fr/resources/atWeb/annotation/>\n" +
//                   "SELECT ?docid WHERE { ?doc rdf:type anno:Document; anno:hasForID ?docid . }";
//        QueryExecution qexec = QueryExecutionFactory.create(q, model);
//
//        ResultSet results = qexec.execSelect() ;
//        for ( ; results.hasNext() ; )
//        {
//            QuerySolution soln = results.nextSolution();
//            Literal l = soln.getLiteral("?docid");
//            docIds.add(l.getInt());
//        }

        docIds.add(1271);
//        docIds.add(1272);
//        docIds.add(1273);

        return docIds;
    }

    private static List<Stats> executeQueries(String pathToQueries) {
        List<Stats> stats = new ArrayList<Stats>();
        List<AtWebQuery> queries = getQueries(pathToQueries);
        List<Integer> docIds = getDocIds();

        for(AtWebQuery query : queries) {
            System.out.println("Executing query " + query.path + "...");

            for(Integer docId : docIds) {
                // Skip document that causes queries to take too long
                if(docId == 1296) continue;

                // Uncomment to run experiments using a single document
                // if(docId != 1271) continue;

                System.out.printf("  Querying docId %d... ", docId);

                AtWebQuery boundQuery = query.bindDocId(docId);
                QueryExecution qexec = tripleStore.executeQuery(boundQuery.query, docId);
                System.out.println("  Query execution object created.");
                List<QuerySolution> solutions = new ArrayList<QuerySolution>();

                long t0 = System.currentTimeMillis();
                ResultSet results = qexec.execSelect();
                while(results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    solutions.add(soln);
                }
                long t = System.currentTimeMillis() - t0;
                stats.add(new Stats(query.path, docId, t));

                System.out.printf("took %d milliseconds.\n", t);

                for(QuerySolution soln : solutions) {
                    System.out.println("    Offending document: " + soln.toString());
                }
            }
        }

        return stats;
    }

    private static void printStats(List<Stats> stats, PrintStream out) {
        out.println("queryPath, docId, milliseconds");
        for(Stats s : stats) {
            out.println(s.queryPath + ", " + s.docId.toString() + ", " + s.milliseconds.toString());
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("triples", true, "Path to file with triples to test against (Turtle format)");
        options.addOption("queries", true, "Path to directory with SPARQL query files (plain-text files)");
        options.addOption("store", true, "Triple store to use. Must be 'memory', 'tdb' or 'sdb' (without quotes).");
        options.addOption("csv", true, "Path to a file in which statistics will be stored in CSV format.");
        options.addOption("tdb", true, "Use an existing TDB store with named graphs for each document.");
        options.addOption("help", "Print help.");
        return options;
    }

    private static CommandLine cli(String[] args) {
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

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("metrics", getOptions());
    }

    public static void main( String[] args )
    {
        CommandLine line = cli(args);

        if(line.hasOption("help") || (!line.hasOption("tdb") && !line.hasOption("triples")) || !line.hasOption("queries")) {
            printHelp();
            return;
        }

        if(line.hasOption("tdb") && line.hasOption("store")) {
            System.out.println("Can't use 'store' and 'tdb' options simultaneously.");
            printHelp();
            return;
        }

        String pathToTdbStore = line.getOptionValue("tdb");
        String pathToTriples = line.getOptionValue("triples");
        String pathToQueries = line.getOptionValue("queries");

        if(pathToTdbStore != null) {
            tripleStore = new PrePopulatedTdbTripleStore();
            tripleStore.open(pathToTdbStore);
        } else {
            String modelType = "tdb";
            String storeOpt = line.getOptionValue("store");
            if (storeOpt == null) {
                modelType = "memory";
            } else if (storeOpt.equals("memory") || storeOpt.equals("tdb") || storeOpt.equals("sdb")) {
                modelType = storeOpt;
            } else {
                printHelp();
                return;
            }

            if(modelType.equals("memory")) {
                System.out.println("Loading in-memory model...");
                tripleStore = new InMemoryTripleStore();
            } else if(modelType.equals("tdb")) {
                System.out.println("Loading TDB model...");
                tripleStore = new TdbTripleStore();
            } else {
                System.out.println("Loading SDB model...");
                tripleStore = new SdbTripleStore();
            }

            tripleStore.open(pathToTriples);
        }

        System.out.printf("Model loaded; %d triples read.\n", tripleStore.getModel().size());

        PrintStream csv = null;
        String csvOpt = line.getOptionValue("csv");
        if(csvOpt != null) try {
            csv = new PrintStream(csvOpt, "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Executing queries...");
        List<Stats> stats = executeQueries(pathToQueries);

        System.out.println("--------------------------------------------------");
        System.out.println("Stats summary:");
        printStats(stats, System.out);

        if(csv != null) printStats(stats, csv);
    }
}
