.PHONY: run-experiments run-10M run-atweb run-bioref run-custom-jena-fn \
        run-sdb \
        plot-10M plot-atweb plot-bioref plot-custom-jena-fn

run-experiments: run-10M run-atweb run-bioref run-custom-jena-fn run-sdb

run-10M:
	./measure.py experiments/10M/triples.ttl \
	             experiments/10M/queries \
	             experiments/10M/results \
	             --tdb --memory -n 3


run-atweb:
	./measure.py experiments/atweb/triples.ttl \
	             experiments/atweb/queries \
	             experiments/atweb/results \
	             --tdb --prepopulatedtdb --memory -n 3

run-bioref:
	./measure.py experiments/bioref/triples.ttl \
	             experiments/bioref/queries \
	             experiments/bioref/results \
	             --tdb --memory -n 3

run-custom-jena-fn:
	./measure.py experiments/custom-jena-fn/triples.ttl \
	             experiments/custom-jena-fn/queries \
	             experiments/custom-jena-fn/results \
	             --tdb --memory -n 3

run-sdb:
	./measure.py experiments/sdb/triples.ttl \
	             experiments/sdb/queries \
	             experiments/sdb/results \
	             --sdb --tdb --memory -n 3

plot-10M:
	./plot.py experiments/10M/results --title "Existing annotations and copies (around 10M triples)"

plot-atweb:
	./plot.py experiments/atweb/results --title "Annotations from the whole AtWeb platform (863048 triples)"

plot-bioref:
	./plot.py experiments/bioref/results --title "Annotations from the whole biorefinery domain (791086 triples)"

plot-custom-jena-fn:
	./plot.py experiments/custom-jena-fn/results --title "Unit conversion using custom Jena functions (1726096 triples)"

plot-sdb:
	./plot.py experiments/sdb/results --title "SDB performance against annotations from the Bioref-PM topic (18790 triples)"
