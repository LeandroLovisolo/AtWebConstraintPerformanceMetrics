package fr.inra.supagro.atweb.constraints.metrics;

class AtWebQuery {
    public String path;
    public String query;

    public AtWebQuery(String path, String query) {
        this.path = path;
        this.query = query;
    }

    public AtWebQuery bindDocId(Integer docId) {
        return new AtWebQuery(path, query.replaceAll("#docid#", docId.toString()));
    }
}
