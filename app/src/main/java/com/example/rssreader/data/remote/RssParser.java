package com.example.rssreader.data.remote;

import android.util.Xml;

import com.example.rssreader.data.model.Article;
import com.example.rssreader.util.DateUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RssParser {

    public List<Article> parse(String xml, long feedId) throws XmlPullParserException, IOException {
        List<Article> articles = new ArrayList<>();
        if (xml == null || xml.trim().isEmpty()) {
            return articles;
        }

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xml));

        int eventType = parser.getEventType();
        Article currentArticle = null;
        String currentTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    currentTag = tagName != null ? tagName.toLowerCase() : "";
                    if ("item".equalsIgnoreCase(tagName) || "entry".equalsIgnoreCase(tagName)) {
                        currentArticle = new Article();
                        currentArticle.setFeedId(feedId);
                    } else if (currentArticle != null) {
                        if ("enclosure".equalsIgnoreCase(tagName)) {
                            String url = parser.getAttributeValue(null, "url");
                            String type = parser.getAttributeValue(null, "type");
                            if (url != null && (type == null || type.startsWith("image") || isImageUrl(url))) {
                                if (currentArticle.getImageUrl() == null || currentArticle.getImageUrl().isEmpty()) {
                                    currentArticle.setImageUrl(url);
                                }
                            }
                        } else if ("media:content".equalsIgnoreCase(tagName) || "content".equalsIgnoreCase(tagName)) {
                            String url = parser.getAttributeValue(null, "url");
                            String medium = parser.getAttributeValue(null, "medium");
                            if (url != null && ("image".equalsIgnoreCase(medium) || isImageUrl(url))) {
                                if (currentArticle.getImageUrl() == null || currentArticle.getImageUrl().isEmpty()) {
                                    currentArticle.setImageUrl(url);
                                }
                            }
                        } else if ("link".equalsIgnoreCase(tagName)) {
                            String href = parser.getAttributeValue(null, "href");
                            if (href != null && !href.isEmpty()) {
                                currentArticle.setLink(href);
                            }
                        }
                    }
                    break;

                case XmlPullParser.TEXT:
                    if (currentArticle != null && !currentTag.isEmpty()) {
                        String text = parser.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            switch (currentTag) {
                                case "title":
                                    if (currentArticle.getTitle() == null) {
                                        currentArticle.setTitle(cleanHtmlText(text));
                                    }
                                    break;
                                case "link":
                                    if (currentArticle.getLink() == null || currentArticle.getLink().isEmpty()) {
                                        currentArticle.setLink(text.trim());
                                    }
                                    break;
                                case "description":
                                case "summary":
                                    if (currentArticle.getDescription() == null) {
                                        parseDescriptionAndExtractImage(text, currentArticle);
                                    }
                                    break;
                                case "pubdate":
                                case "published":
                                case "updated":
                                    if (currentArticle.getPubDate() <= 0) {
                                        currentArticle.setPubDate(DateUtils.parseRssDate(text));
                                    }
                                    break;
                            }
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (("item".equalsIgnoreCase(tagName) || "entry".equalsIgnoreCase(tagName)) && currentArticle != null) {
                        if (currentArticle.getTitle() != null && currentArticle.getLink() != null) {
                            if (currentArticle.getPubDate() <= 0) {
                                currentArticle.setPubDate(System.currentTimeMillis());
                            }
                            articles.add(currentArticle);
                        }
                        currentArticle = null;
                    }
                    currentTag = "";
                    break;
            }

            eventType = parser.next();
        }

        return articles;
    }

    private void parseDescriptionAndExtractImage(String rawHtml, Article article) {
        if (rawHtml == null) return;
        try {
            Document doc = Jsoup.parse(rawHtml);
            if (article.getImageUrl() == null || article.getImageUrl().isEmpty()) {
                Element img = doc.select("img").first();
                if (img != null) {
                    String src = img.attr("src");
                    if (src.isEmpty()) {
                        src = img.attr("data-original");
                    }
                    if (!src.isEmpty()) {
                        article.setImageUrl(src);
                    }
                }
            }
            String cleanText = doc.text();
            article.setDescription(cleanText);
        } catch (Exception e) {
            article.setDescription(rawHtml);
        }
    }

    private String cleanHtmlText(String raw) {
        if (raw == null) return "";
        try {
            return Jsoup.parse(raw).text();
        } catch (Exception e) {
            return raw.trim();
        }
    }

    private boolean isImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".webp") || lower.endsWith(".gif") || lower.contains("image");
    }
}
