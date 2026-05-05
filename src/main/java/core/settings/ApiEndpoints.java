package core.settings;

public enum ApiEndpoints {
    PING("/ping"),
    BOOKING("/booking"),
    AUTH("/auth"),
    BOOKING_BY_ID("/booking/%d");

    private final String path;

    ApiEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getPathById(int id) {
        return path + "/" + id;
    }
}
