#!/usr/bin/env python2

LOGSCALE=True
# LOGSCALE=False

import numpy as np
import matplotlib.pyplot as plt
import csv

class Query:
  def __init__(self, queryPath):
    self.queryPath = queryPath
    self.runTimes = []

  def addRunTime(self, runTime):
    self.runTimes.append(runTime)

  def mean(self):
    return np.mean(self.runTimes)

  def std(self):
    return np.std(self.runTimes)

def loadQueries(store):
  files = []
  for i in range(1, 6):
    files.append("results/results-%s-%d.csv" % (store, i))

  queries = {}

  for file in files:
    with open(file, "r") as csvfile:
      reader = csv.reader(csvfile, delimiter=",", quotechar='"')
      isFirst = True
      for row in reader:
        if isFirst:
          isFirst = False
        else:
          if not queries.has_key(row[0]):
            queries[row[0]] = Query(row[0])
          queries[row[0]].addRunTime(int(row[2]))

  return queries

queriesMemory = loadQueries("memory")
queriesTdb = loadQueries("tdb")




labels = []
memoryMeans = []
memoryStd = []
tdbMeans = []
tdbStd = []

for key in queriesMemory.keys():
  labels.append(key.split("/")[2].split(".")[0])
  memoryMeans.append(queriesMemory[key].mean())
  memoryStd.append(queriesMemory[key].std())
  tdbMeans.append(queriesTdb[key].mean())
  tdbStd.append(queriesTdb[key].std())

print labels


N = len(memoryMeans)

ind = np.arange(N)  # the x locations for the groups
width = 0.35       # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, memoryMeans, width, color='r', yerr=memoryStd, log=LOGSCALE)
rects2 = ax.bar(ind + width, tdbMeans, width, color='y', yerr=tdbStd, log=LOGSCALE)

# add some text for labels, title and axes ticks
ax.set_xlabel('Query')
ax.set_ylabel('Time (ms)')
ax.set_title('Query execution times')
ax.set_xticks(ind + width)
ax.set_xticklabels(labels)

ax.legend((rects1[0], rects2[0]), ('Memory', 'TDB'))

def autolabel(rects):
    # attach some text labels
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x() + rect.get_width()/2., 1.05*height,
                '%d' % int(height),
                ha='center', va='bottom')

autolabel(rects1)
autolabel(rects2)

plt.show()
