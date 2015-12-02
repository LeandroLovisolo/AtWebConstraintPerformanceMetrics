package fr.inra.supagro.atweb.scripts;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.util.Iterator;

public class ListNamedGraphs {

    public static void main(String[] args) {
        System.out.println("Generating list of named graphs...");

        Dataset dataset = TDBFactory.createDataset("TDB_atWeb/annot_dataset");

        Iterator<String> it = dataset.listNames();
        while(it.hasNext()) {
            String name = it.next();
            System.out.println(name);

            Model model = dataset.getNamedModel(name);
            System.out.printf("Model size: %d\n", model.size());
        }
    }

}
