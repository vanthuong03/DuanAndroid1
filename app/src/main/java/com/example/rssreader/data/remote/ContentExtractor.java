package com.example.rssreader.data.remote;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ContentExtractor {

    private final OkHttpClient client;

    // Selector priority for Vietnamese news and standard blogs
    private static final String[] CONTENT_SELECTORS = {
            "article",
            "[itemprop=articleBody]",
            ".fck_detail",         // VnExpress
            ".detail-content",      // Tuoi Tre
            ".detail-cmain",        // Thanh Nien
            ".content-detail",      // Common news
            ".entry-content",       // WordPress / standard blog
            ".post-content",
            ".article-content",
            "#main-content"
    };

    public ContentExtractor() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    public String extractArticleContent(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error code: " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty HTML body");
            }
            String html = body.string();
            return parseAndCleanHtml(html, url);
        }
    }

    public String parseAndCleanHtml(String html, String baseUri) {
        Document doc = Jsoup.parse(html, baseUri);

        // Remove unwanted elements
        doc.select("script, style, iframe, nav, aside, form, header, footer, noscript, svg").remove();
        doc.select(".advertisement, .ads, .banner, .box-quang-cao, .social-share, .author-info, .comment-box, .box-category").remove();

        Element mainContainer = null;

        // Try selectors in priority order
        for (String selector : CONTENT_SELECTORS) {
            Element el = doc.selectFirst(selector);
            if (el != null && el.select("p").size() >= 2) {
                mainContainer = el;
                break;
            }
        }

        // Fallback: search for container with highest count of <p> tags
        if (mainContainer == null) {
            Elements candidates = doc.select("div, section, main");
            int maxPCount = 0;
            for (Element candidate : candidates) {
                int pCount = candidate.select("p").size();
                if (pCount > maxPCount) {
                    maxPCount = pCount;
                    mainContainer = candidate;
                }
            }
        }

        if (mainContainer == null) {
            mainContainer = doc.body();
        }

        // Remove again any nested garbage within mainContainer
        mainContainer.select("script, style, iframe, form, button, input").remove();

        // Fix relative image URLs
        for (Element img : mainContainer.select("img")) {
            String src = img.absUrl("src");
            if (src.isEmpty()) {
                src = img.absUrl("data-original");
            }
            if (src.isEmpty()) {
                src = img.absUrl("data-src");
            }
            if (!src.isEmpty()) {
                img.attr("src", src);
            }
        }

        // Build a clean, structured HTML output for reader
        StringBuilder sb = new StringBuilder();
        Elements elements = mainContainer.select("h1, h2, h3, h4, p, img, blockquote, ul, ol");
        for (Element el : elements) {
            String tag = el.tagName().toLowerCase();
            if ("img".equals(tag)) {
                String src = el.attr("src");
                if (!src.isEmpty()) {
                    sb.append("<p style=\"text-align:center;\"><img src=\"").append(src).append("\" style=\"max-width:100%; border-radius:8px; margin:12px 0;\" /></p>");
                }
            } else if ("p".equals(tag)) {
                String text = el.text().trim();
                if (!text.isEmpty()) {
                    sb.append("<p style=\"line-height:1.6; margin-bottom:14px;\">").append(text).append("</p>");
                }
            } else if (tag.startsWith("h")) {
                String text = el.text().trim();
                if (!text.isEmpty()) {
                    sb.append("<").append(tag).append(" style=\"margin-top:18px; margin-bottom:10px;\">").append(text).append("</").append(tag).append(">");
                }
            } else if ("blockquote".equals(tag)) {
                String text = el.text().trim();
                if (!text.isEmpty()) {
                    sb.append("<blockquote style=\"border-left:4px solid #1A73E8; padding-left:12px; margin:14px 0; font-style:italic;\">").append(text).append("</blockquote>");
                }
            } else if ("ul".equals(tag) || "ol".equals(tag)) {
                sb.append(el.outerHtml());
            }
        }

        String result = sb.toString();
        if (result.trim().isEmpty()) {
            return "<p>" + mainContainer.text() + "</p>";
        }
        return result;
    }
}
