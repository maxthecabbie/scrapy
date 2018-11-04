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

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPicLimit() {
        return this.picLimit;
    }

    public void setPicLimit(int picLimit) {
        this.picLimit = picLimit;
    }
}