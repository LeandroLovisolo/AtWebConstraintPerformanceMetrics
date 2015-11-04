#!/usr/bin/env python

from subprocess import call

if __name__ == "__main__":
  stores = ["memory", "tdb"]
  runs = 5

  for run in range(1, runs + 1):
    for store in stores:
      csv = "results/results-%s-%d.csv" % (store, run)
      command = "mvn exec:java " + \
                "-Dexec.mainClass=\"fr.inra.supagro.atweb.constraints.metrics.App\" " + \
                "-Dexec.args=\"%s %s\"" % (store, csv)
      print command
      call(command, shell=True)
