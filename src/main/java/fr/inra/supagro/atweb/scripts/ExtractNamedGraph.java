package fr.inra.supagro.atweb.scripts;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ExtractNamedGraph {
    public static void main(String[] args) {
        String namedGraph = "Document_1271";
        Dataset dataset = TDBFactory.createDataset("TDB_atWeb/annot_dataset");
        Model model = dataset.getNamedModel(namedGraph);
        System.out.printf("Named graph: %s. Size: %d triples.\n", namedGraph, model.size());

        try {
            model.write(new FileOutputStream(namedGraph + ".ttl"), "TURTLE");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dataset.close();
    }
}
