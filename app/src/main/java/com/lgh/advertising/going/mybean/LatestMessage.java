package com.lgh.advertising.going.mybean;

import java.util.List;

public class LatestMessage {

    public String url, assets_url, upload_url, html_url, id, node_id, tag_name, target_commitish, name, created_at, published_at, tarball_url, zipball_url, body;
    public boolean draft, prerelease;
    public Author author;
    public List<Asset> assets;

    public static class Asset {
        public String url, id, node_id, name, label, content_type, state, size, download_count, created_at, updated_at, browser_download_url;
        public Uploader uploader;
    }

    public static class Author {
        public String login, id, node_id, avatar_url, gravatar_id, url, html_url, followers_url, following_url, gists_url, starred_url, subscriptions_url, organizations_url, repos_url, events_url, received_events_url, type, site_admin;
    }

    public static class Uploader {
        public String login, id, node_id, avatar_url, gravatar_id, url, html_url, followers_url, following_url, gists_url, starred_url, subscriptions_url, organizations_url, repos_url, events_url, received_events_url, type, site_admin;
    }
}
