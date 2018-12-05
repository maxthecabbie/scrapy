package scrapy.utils;

public class RequestBodyData {
    private String url;
    private int picLimit;

    public RequestBodyData(String url, int picLimit) {
        this.url = url;
        this.picLimit = picLimit;
    }

    public String getUrl() {
        return this.url;
    }

    public int getPicLimit() {
        return this.picLimit;
    }
}