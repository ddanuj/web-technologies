package edu.usc.cs_server.stockinfo.model;

/**
 * Created by Anuj Doiphode on 19-11-2017.
 */

public class StockNewsDataModel {
    private String title;
    private String link;
    private String pubDate;
    private String authorName;

    public StockNewsDataModel(String title, String link, String pubDate, String authorName) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getAuthorName() {
        return authorName;
    }
}
