package fr.inra.supagro.atweb.constraints.metrics;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class App
{
    private static class AtWebQuery {
        public String path;
        public String query;

        public AtWebQuery(String path, String query) {
            this.path = path;
            this.query = query;
        }

        public AtWebQuery bindDocId(Integer docId) {
            return new AtWebQuery(path, query.replaceAll("#docid#", docId.toString()));
        }
    }

    private static class Stats {
        public String queryPath;
        public Integer docId;
        public Long milliseconds;

        public Stats(String queryPath, Integer docId, Long milliseconds) {
            this.queryPath = queryPath;
            this.docId = docId;
            this.milliseconds = milliseconds;
        }
    }

    private static Dataset dataset;

    private static Model loadInMemoryModel()
    {
        Model model = ModelFactory.createDefaultModel();
        model.read(App.class.getResourceAsStream("/annotations-small.ttl"), null, FileUtils.langTurtle);
        model.read(App.class.getResourceAsStream("/IC2ACV.owl"), null);
        return model;
    }

    private static Model loadTdbModel() {
        Dataset dataset = TDBFactory.createDataset("tdb");
        Model model = dataset.getDefaultModel();

        // If model is empty, populate it with annotations and biorefinery ontology triples
        if(model.size() == 0) {
            model.read(App.class.getResourceAsStream("/annotations-small.ttl"), null, FileUtils.langTurtle);
            model.read(App.class.getResourceAsStream("/IC2ACV.owl"), null);
        }

        return model;
    }

    private static List<AtWebQuery> getQueries() {
        List<AtWebQuery> queries = new ArrayList<AtWebQuery>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                org.apache.commons.io.FileUtils.getFile("queries"),
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

    private static List<Integer> getDocIds(Model model) {
        List<Integer> docIds = new ArrayList<Integer>();

        String q = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                   "prefix anno: <http://opendata.inra.fr/resources/atWeb/annotation/>\n" +
                   "SELECT ?docid WHERE { ?doc rdf:type anno:Document; anno:hasForID ?docid . }";
        QueryExecution qexec = QueryExecutionFactory.create(q, model);

        ResultSet results = qexec.execSelect() ;
        for ( ; results.hasNext() ; )
        {
            QuerySolution soln = results.nextSolution();
            Literal l = soln.getLiteral("?docid");
            docIds.add(l.getInt());
        }

        return docIds;
    }

    private static List<Stats> executeQueries(Model model) {
        List<Stats> stats = new ArrayList<Stats>();
        List<AtWebQuery> queries = getQueries();
        List<Integer> docIds = getDocIds(model);

        for(AtWebQuery query : queries) {
            System.out.println("Executing query " + query.path + "...");

            for(Integer docId : docIds) {
                // Skip document that causes queries to take too long
                if(docId == 1296) continue;

                // Uncomment to run experiments using a single document
                // if(docId != 1271) continue;

                System.out.printf("  Querying docId %d... ", docId);

                AtWebQuery boundQuery = query.bindDocId(docId);
                QueryExecution qexec = QueryExecutionFactory.create(boundQuery.query, model);

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

    private static void printHelp() {
        System.out.println("Accepted command-line arguments: [store] [csv]");
        System.out.println("  where: [store] is either 'memory' or 'tdb' (without quotes)");
        System.out.println("         [csv]   is a path where stats will be written in CSV format");
    }

    public static void main( String[] args )
    {
        if(args.length > 2) {
            printHelp();
            return;
        }

        String modelType = "memory";
        if(args.length >= 1) {
            if(!args[0].equals("memory") && !args[0].equals("tdb")) {
                printHelp();
                return;
            }
            modelType = args[0];
        }

        PrintStream csv = null;
        if(args.length == 2) try {
            csv = new PrintStream(args[1], "utf-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        Model model;
        if(modelType.equals("memory")) {
            System.out.println("Loading in-memory model...");
            model = loadInMemoryModel();
        } else {
            System.out.println("Loading TDB model...");
            model = loadTdbModel();
        }
        System.out.printf("Model loaded; %d triples read.\n", model.size());

        System.out.println("Executing queries...");
        List<Stats> stats = executeQueries(model);

        System.out.println("--------------------------------------------------");
        System.out.println("Stats summary:");
        printStats(stats, System.out);

        if(csv != null) printStats(stats, csv);

        if(dataset != null) dataset.close();
    }
}
