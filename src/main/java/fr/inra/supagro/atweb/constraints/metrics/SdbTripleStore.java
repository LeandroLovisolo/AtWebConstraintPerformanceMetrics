package fr.inra.supagro.atweb.constraints.metrics;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.store.DatasetStore;
import org.apache.jena.util.FileUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SdbTripleStore implements TripleStore {
    private Store store;
    private Model model;
    private Dataset dataset;

    private void resetDb() {
        // Retrieve database parameters
        Model model = ModelFactory.createDefaultModel();
        InputStream ttlFileStream = null;
        try {
            ttlFileStream = new FileInputStream("sdb.ttl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        model.read(ttlFileStream, null, FileUtils.langTurtle);
        String q = "prefix sdb:  <http://jena.hpl.hp.com/2007/sdb#> \n" +
                   "SELECT ?host ?dbname ?dbuser WHERE {\n" +
                   "    <#conn> a sdb:SDBConnection ;\n" +
                   "            sdb:sdbType \"postgresql\" ;\n" +
                   "            sdb:sdbHost ?host ;\n" +
                   "            sdb:sdbName ?dbname ;\n" +
                   "            sdb:sdbUser ?dbuser .\n" +
                   "}";
        QueryExecution qexec = QueryExecutionFactory.create(q, model);
        ResultSet results = qexec.execSelect() ;
        QuerySolution soln = results.nextSolution();
        String host = soln.getLiteral("?host").getString();
        String dbname = soln.getLiteral("?dbname").getString();
        String dbuser = soln.getLiteral("?dbuser").getString();
        model.close();

        // Load PostgreSQL driver as per http://www.postgresql.org/docs/7.4/static/jdbc-use.html
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Empty database tables
        try {
            Connection db = DriverManager.getConnection("jdbc:postgresql://" + host + "/" + dbname, dbuser, "");
            Statement stmt = db.createStatement();
            stmt.executeUpdate("DELETE FROM triples");
            stmt.executeUpdate("DELETE FROM nodes");
            stmt.executeUpdate("DELETE FROM prefixes");
            stmt.executeUpdate("DELETE FROM quads");
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void open(String pathToTtlFile) {
        resetDb();

        store = SDBFactory.connectStore("sdb.ttl") ;
        model = SDBFactory.connectDefaultModel(store) ;

        // Must be a DatasetStore to trigger the SDB query engine.
        // Creating a graph from the Store, and adding it to a general
        // purpose dataset will not necesarily exploit full SQL generation.
        // The right answers will be obtained but slowly.

        dataset = DatasetStore.create(store) ;

        // If model is empty, populate it with annotations and biorefinery ontology triples
        if(model.size() == 0) {
            System.out.println("Populating model");
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
    }

    public void close() {
        store.getConnection().close() ;
        store.close() ;
    }

    public QueryExecution executeQuery(String q, Integer docId) {
        return QueryExecutionFactory.create(q, dataset);
    }

    public Model getModel() {
        return model;
    }
}