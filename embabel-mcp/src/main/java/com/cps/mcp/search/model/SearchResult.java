package com.cps.mcp.search.model;

/**
 * A provider-agnostic search result entry.
 */
public class SearchResult {
    private final String title;
    private final String url;
    private final String content;

    public SearchResult(String title, String url, String content) {
        this.title = title != null ? title : "";
        this.url = url != null ? url : "";
        this.content = content != null ? content : "";
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "SearchResult{title='" + title + "', url='" + url + "', content='" + content + "'}";
    }
}
