package accsswap;

public final class AccsEntry {
    public final int id;
    public final String name;
    public final String uniqname;
    public final boolean privately;
    public final int partCount;

    public AccsEntry(int id, String name, String uniqname, boolean privately, int partCount) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.uniqname = uniqname == null ? "" : uniqname;
        this.privately = privately;
        this.partCount = partCount;
    }

    public String display() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("  ").append(name);
        if (uniqname.length() > 0 && !uniqname.equals(name)) {
            sb.append("  [").append(uniqname).append("]");
        }
        if (privately) {
            sb.append("  (\u5185\u90e8)");
        }
        sb.append("  parts=").append(partCount);
        return sb.toString();
    }

    public String toString() {
        return display();
    }

    public boolean matches(String query) {
        if (query == null) {
            return false;
        }
        String q = query.trim();
        if (q.length() == 0) {
            return false;
        }
        if (q.matches("\\d+")) {
            return String.valueOf(id).startsWith(q) || id == Integer.parseInt(q);
        }
        String lower = q.toLowerCase();
        return name.toLowerCase().indexOf(lower) >= 0
                || uniqname.toLowerCase().indexOf(lower) >= 0
                || String.valueOf(id).indexOf(q) >= 0;
    }
}
