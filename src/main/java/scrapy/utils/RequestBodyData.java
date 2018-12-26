package scrapy.utils;

public class RequestBodyData {
    private String yelpUrl;
    private String type;
    private int picLimit;

    public RequestBodyData(String yelpUrl, String type, int picLimit) {
        this.yelpUrl = yelpUrl;
        this.type = type;
        this.picLimit = picLimit;
    }

    public String getYelpUrl() {
        return yelpUrl;
    }

    public String getType() {
        return type;
    }

    public int getPicLimit() {
        return picLimit;
    }
}