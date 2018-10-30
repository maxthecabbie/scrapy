package scrapy.utils;

public class RequestBodyData {
    private String url;
    private Integer picLimit;

    public RequestBodyData(String url, Integer picLimit) {
        this.url = url;
        this.picLimit = picLimit;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPicLimit() {
        return this.picLimit;
    }

    public void setPicLimit(Integer picLimit) {
        this.picLimit = picLimit;
    }
}