package fr.inra.supagro.atweb.constraints.metrics;

class Stats {
    public String queryPath;
    public Integer docId;
    public Long milliseconds;

    public Stats(String queryPath, Integer docId, Long milliseconds) {
        this.queryPath = queryPath;
        this.docId = docId;
        this.milliseconds = milliseconds;
    }
}
