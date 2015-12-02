#!/usr/bin/env python

# This script prints the number of triples in an RDF graph file.
#
# Requires RDFLib (https://github.com/RDFLib/rdflib). To install it, execute
# the following command:
#
#   sudo easy_install rdflib
#
# Author: Leandro Lovisolo <leandro@leandro.me>
# 2015-11-16

import argparse
import rdflib

def countTriples(path):
  g = rdflib.Graph()
  print "Parsing %s..." % path
  g.parse(path, format="turtle")
  print "Graph contains %d triples." % len(g)

if __name__ == "__main__":
  parser = argparse.ArgumentParser(
      description="Counts the number of triples in an RDF graph file.")
  parser.add_argument("path", help="Path to source graph file (Turtle syntax)")
  args = parser.parse_args()
  countTriples(args.path)
