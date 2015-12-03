# AtWebConstraintPerformanceMetrics

Performance tests for the constraints implemented during my internship period at [INRA SupAgro](http://www.supagro.inra.fr/) in Montpellier, France (September 2015 - February 2016)

## Introduction

The goal of this code is to compare the running times of some sample SPARQL queries against different triple store implementations provided by the Jena library using datasets of different sizes.

The set of SPARQL queries used in this experiment are located in the `queries` directory. Datasets are located in the `triples` directory.

### Datasets

The following list gives a brief explanation of each dataset used thrughout the experiments.

- `annotations-bioref-pm.ttl`: annotations of the subset of documents in the biorefinery domain which contain experiments in the "Bioref-PM" topic. Size: 18790 triples.

- `annotations-bioref.ttl`: annotations of all the documents in the biorefinery domain. Size: 791086 triples.

- `annotations-atweb.ttl`: the complete set of all annotations in the @Web platform. Size: 863048 triples.

- `annotations-1M.ttl`: complete set of annotations in the @Web platform, plus multiple copies with different URIs created to increment the triple count. Size: around 1 million triples.

- `annotations-10M.ttl`: same as `annotations-1M.ttl`, with a larger triple count. Size: around 10 million triples.

## Experiments

Each experiment is explained in the following subsections.

### bioref

Triple store implementations compared:

- In-memory store.
- TDB.

Dataset used: `annotations-bioref.ttl`.

### atweb

Triple store implementations compared:

- In-memory store.
- TDB.
- Prepopulated TDB store (a copy of the actual TDB store used by @Web.)

Dataset used: `annotations-atweb.ttl`.

### 10M

Triple store implementations compared:

- In-memory store.
- TDB.

Dataset used: `annotations-10M.ttl`.

### custom-jena-fn

Two versions of the same query are compared. One uses a custom SPARQL function written in Java to simulate unit conversion code, while the other doesn't.

Triple store implementations compared:

- In-memory store.
- TDB.

Dataset used: `annotations-1M.ttl`.

### sdb

Triple store implementations compared:

- In-memory store.
- TDB.
- SDB.

Dataset used: `annotations-bioref-pm.ttl`.

## Running the experiments

Please note that the experiment results are already included in this repository. If you wish to see the results without actually reproducing the experiments, skip to "Plotting results".

### Requirements

First of all, you need a dump of the @Web TDB store containing the annotations. Then you must place it in the `/TDB_atWeb/annot_dataset` directory relative to the project's root. To make sure you've placed it in the right location, you should see something like this when you list the files in `/TDB_atWeb/annot_dataset`:

```
$ ls TDB_atWeb/annot_dataset
GOSP.dat      nodes.dat  prefix2id.dat
GOSP.idn      OSP.dat    prefix2id.idn
GPOS.dat      OSPG.dat   prefixes.dat
GPOS.idn      OSPG.idn   prefixIdx.dat
GSPO.dat      OSP.idn    prefixIdx.idn
GSPO.idn      POS.dat    SPO.dat
journal.jrnl  POSG.dat   SPOG.dat
node2id.dat   POSG.idn   SPOG.idn
node2id.idn   POS.idn    SPO.idn
```

Next, you need to run `make uncompress` from the project's root to uncompress the dataset files in the `/triples` directory.

After that, run `mvn install` from the project's root to retrieve the Java dependencies and build the project. Note that a valid [Maven](http://maven.apache.org/) installation is required for this step.

### Reproducing the experiments

At this point you're ready to run the actual experiments.

There's a Makefile target for each experiment:

```
make run-bioref
make run-atweb
make run-10M
make run-custom-jena-fn
make run-sdb
```

There's also a target for running all the experiments one after the other:

```
make run-experiments
```

Have in mind that some of these experiments might take hours to finish. You might want to run `make run-experiments` overnight.

## Plotting results

There's a Makefile target for plotting the results of each the experiment:

```
make plot-bioref
make plot-atweb
make plot-10M
make plot-custom-jena-fn
make plot-sdb
```

Note that [Python](https://www.python.org/) and [matplotlib](http://matplotlib.org/) are required to execute the plotting scripts.
