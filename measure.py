#!/usr/bin/env python

import argparse
import sys
import glob
from subprocess import call

# if __name__ == "__main__":
#   stores = ["memory", "tdb"]
#   runs = 5
#
#   for run in range(1, runs + 1):
#     for store in stores:
#       csv = "results/results-%s-%d.csv" % (store, run)
#       command = "export MAVEN_OPTS=-Xmx8192m; " + \
#                 "mvn exec:java " + \
#                 "-Dexec.mainClass=\"fr.inra.supagro.atweb.constraints.metrics.App\" " + \
#                 "-Dexec.args=\"%s %s\"" % (store, csv)
#       print command
#       call(command, shell=True)

if __name__ == "__main__":
  parser = argparse.ArgumentParser(description="This script executes an experiment and measure the execution time.",
                                   epilog="Note that at least one triple store is required to run an experiment.")

  parser.add_argument("triples", help="Path to file with triples to test against (Turtle format)")
  parser.add_argument("queries", help="Path to directory with SPARQL query files (plain-text)")
  parser.add_argument("results", help="Path to directory in which to store experiment results data (CSV)")
  parser.add_argument("-n", help="Number of times each experiment should be executed (default: 1)", type=int, default=1)
  parser.add_argument("--memory", action="store_const", const=True, default=False,
                      help="Do an experiment using Jena's in-memory triple store")
  parser.add_argument("--tdb", action="store_const", const=True, default=False,
                      help="Do an experiment using Jena's TDB triple store")
  parser.add_argument("--prepopulatedtdb", action="store_const", const=True, default=False,
                      help="Do an experiment using a prepopulated TDB triple store")
  parser.add_argument("--sdb", action="store_const", const=True, default=False,
                      help="Do an experiment using Jena's SDB triple store")
  args = parser.parse_args()

  if args.n < 1:
    print "Error: must have a number of execution times greater than 0."
    sys.exit(-1)

  stores = []
  if args.memory: stores.append(("memory", "InMemoryExperiment"))
  if args.tdb: stores.append(("tdb", "TdbExperiment"))
  if args.prepopulatedtdb: stores.append(("prepopulatedtdb", "PrePopulatedTdbExperiment"))
  if args.sdb: stores.append(("sdb", "SdbExperiment"))

  if stores == []:
    print "Error: you need to specify at least one triple store."
    sys.exit(-1)

  for i in range(1, args.n + 1):
    for store in stores:
      csv = "%s/results-%s-%d.csv" % (args.results, store[0], i)
      javaArgs = "-queries %s -csv %s" % (args.queries, csv)
      if store[0] == "prepopulatedtdb":
        javaArgs += " -tdbstore TDB_atWeb/annot_dataset"
      else:
        javaArgs += " -triples %s" % args.triples
      command = "export MAVEN_OPTS=-Xmx8192m; " + \
                "mvn exec:java -Dexec.mainClass=\"%s\" -Dexec.args=\"%s\"" % \
                  ("fr.inra.supagro.atweb.constraints.metrics." + store[1],
                   javaArgs)
      print command
      call(command, shell=True)
