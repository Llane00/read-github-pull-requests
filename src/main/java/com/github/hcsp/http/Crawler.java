package com.github.hcsp.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;

public class Crawler {
    static class GitHubPullRequest {
        // Pull request的编号
        int number;
        // Pull request的标题
        String title;
        // Pull request的作者的 GitHub 用户名
        String author;

        GitHubPullRequest(int number, String title, String author) {
            this.number = number;
            this.title = title;
            this.author = author;
        }
    }

    // 给定一个仓库名，例如"golang/go"，或者"gradle/gradle"，返回第一页的Pull request信息
    public static ArrayList<GitHubPullRequest> getFirstPageOfPullRequests(String repo) throws IOException {
        ArrayList<GitHubPullRequest> PullRequestsArray = new ArrayList<GitHubPullRequest>();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://github.com/" + repo + "/pulls?page=1");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            HttpEntity entity = response.getEntity();
            InputStream tmpInputStream = entity.getContent();
            String tmpInputStreamString = IOUtils.toString(tmpInputStream, "utf-8");
            Document tmpInputStreamHtmlDocumentObj = Jsoup.parse(tmpInputStreamString);
            ArrayList<Element> tmpInputStreamDomArrayList = tmpInputStreamHtmlDocumentObj.select(".js-issue-row");

            for (Element item : tmpInputStreamDomArrayList) {
                Integer number = Integer.valueOf(item.select(".opened-by").text().split("\\s+")[0].replace("#", ""));
                String author = item.select("a.muted-link").text().split("\\s+")[0];
                String title = item.select("a.js-navigation-open").text();
                GitHubPullRequest pullRequestItem = new GitHubPullRequest(number, title, author);
                PullRequestsArray.add(pullRequestItem);
            }
        } finally {
            response.close();
        }
        return PullRequestsArray;
    }
}
