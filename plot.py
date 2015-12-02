#!/usr/bin/env python2

import argparse
import sys
import glob
import re
import csv
import ntpath
import numpy as np
import matplotlib.pyplot as plt

# class Query:
#   def __init__(self, queryPath):
#     self.queryPath = queryPath
#     self.runTimes = []
#
#   def addRunTime(self, runTime):
#     self.runTimes.append(runTime)
#
#   def mean(self):
#     return np.mean(self.runTimes)
#
#   def std(self):
#     return np.std(self.runTimes)
#
# def loadQueries(directory, store):
#   files = []
#   for i in range(1, 6):
#     files.append(os.path.join(directory, "results-%s-%d.csv" % (store, i)))
#
#   queries = {}
#
#   for file in files:
#     with open(file, "r") as csvfile:
#       reader = csv.reader(csvfile, delimiter=",", quotechar='"')
#       isFirst = True
#       for row in reader:
#         if isFirst:
#           isFirst = False
#         else:
#           if not queries.has_key(row[0]):
#             queries[row[0]] = Query(row[0])
#           queries[row[0]].addRunTime(int(row[2]))
#
#   return queries
#
# def doPlot(directory, logscale):
#   queriesMemory = loadQueries(directory, "memory")
#   queriesTdb = loadQueries(directory, "tdb")
#
#   labels = []
#   memoryMeans = []
#   memoryStd = []
#   tdbMeans = []
#   tdbStd = []
#
#   for key in queriesMemory.keys():
#     labels.append(key.split("/")[2].split(".")[0])
#     memoryMeans.append(queriesMemory[key].mean())
#     memoryStd.append(queriesMemory[key].std())
#     tdbMeans.append(queriesTdb[key].mean())
#     tdbStd.append(queriesTdb[key].std())
#
#   N = len(memoryMeans)
#
#   ind = np.arange(N)  # the x locations for the groups
#   width = 0.35        # the width of the bars
#
#   fig, ax = plt.subplots()
#   rects1 = ax.bar(ind, memoryMeans, width, color='r', yerr=memoryStd, log=logscale)
#   rects2 = ax.bar(ind + width, tdbMeans, width, color='y', yerr=tdbStd, log=logscale)
#
#   # add some text for labels, title and axes ticks
#   ax.set_xlabel('Query')
#   ax.set_ylabel('Time (ms)')
#   ax.set_title('Query execution times')
#   ax.set_xticks(ind + width)
#   ax.set_xticklabels(labels)
#
#   ax.legend((rects1[0], rects2[0]), ('Memory', 'TDB'))
#
#   def autolabel(rects):
#       # attach some text labels
#       for rect in rects:
#           height = rect.get_height()
#           ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
#                   '%d' % int(height),
#                   ha='center', va='bottom')
#
#   autolabel(rects1)
#   autolabel(rects2)
#
#   plt.show()

class Query:
  def __init__(self):
    self.runTimes = []

  def addRunTime(self, runTime):
    self.runTimes.append(runTime)

  def mean(self):
    return np.mean(self.runTimes)

  def std(self):
    return np.std(self.runTimes)

def loadMetrics(directory):
  # Dictionary of dictionaries of the following form:
  #   {store -> {queryPath -> Query class instance}}
  metrics = {}

  # Parse CSV files
  for file in glob.glob(directory + "/*.csv"):
    m = re.search("results-(memory|tdb|prepopulatedtdb|sdb)-(\d+).csv", file)
    if m is not None:
      store = m.group(1)
      if not metrics.has_key(store):
        metrics[store] = {}
      with open(file, "r") as csvfile:
        reader = csv.reader(csvfile, delimiter=",", quotechar='"')
        isFirst = True
        for row in reader:
          # Skip header
          if isFirst: isFirst = False
          else:
            queryPath = row[0]
            docId = row[1] # not used
            milliseconds = int(row[2])
            if not metrics[store].has_key(queryPath):
              metrics[store][queryPath] = Query()
            metrics[store][queryPath].addRunTime(milliseconds)

  # Check that measurements use the same set of queryPaths for each store
  queryPaths = None
  for store in metrics.keys():
    if queryPaths is None:
      queryPaths = metrics[store].keys()
    if metrics[store].keys() != queryPaths:
      print "ERROR: Experiments don't use the same set of queryPaths"
      sys.exit(-1)

  return metrics, queryPaths

def doPlot(directory, title, logscale):
  metrics, queryPaths = loadMetrics(directory)

  labels = map(lambda x: ntpath.basename(x), queryPaths)
  N = len(queryPaths)
  ind = np.arange(N)       # the x locations for the groups
  width = 0.35             # the width of the bars
  colors = ['r', 'y', 'b'] # available colors

  fig, ax = plt.subplots()
  stores = []
  rects = []

  i = 0
  for store in metrics.keys():
    stores.append(store)
    means = []
    stds = []
    for queryPath in queryPaths:
      means.append(metrics[store][queryPath].mean())
      stds.append(metrics[store][queryPath].std())
    rects.append(ax.bar(ind + (width * i), means, width, color=colors[i],
                        yerr=stds, log=logscale))
    i += 1

  # add some text for labels, title and axes ticks
  ax.set_xlabel('Query')
  ax.set_ylabel('Time (ms)')
  ax.set_title(title)
  ax.set_xticks(ind + width)
  ax.set_xticklabels(labels)

  ax.legend(tuple(map(lambda x: x[0], rects)), tuple(stores))

  def autolabel(rects):
    # attach some text labels
    for rect in rects:
      height = rect.get_height()
      ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
              '%d' % int(height),
              ha='center', va='bottom')

  for rect in rects: autolabel(rect)

  plt.show()


if __name__ == "__main__":
  parser = argparse.ArgumentParser(description="Plots results of constraint performance measurements")
  parser.add_argument("directory", help="Path to directory with measurement results files")
  parser.add_argument("--logscale", dest="logscale", action="store_const", const=True, default=False,
                      help="Plot using logarithmic scale for the Y axis")
  parser.add_argument("--title", default="Query execution times", help="Plot title")
  args = parser.parse_args()
  doPlot(args.directory, args.title, args.logscale)
