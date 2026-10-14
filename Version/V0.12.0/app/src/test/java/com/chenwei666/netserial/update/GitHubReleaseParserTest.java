package com.chenwei666.netserial.update;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitHubReleaseParserTest {
    @Test public void selectsFirstGithubApkAndComparesLatest() {
        String json = "{\"tag_name\":\"v0.6.0\",\"name\":\"V0.6.0\","
                + "\"html_url\":\"https://github.com/chenwei666/NetSerial-AI/releases/tag/v0.6.0\","
                + "\"prerelease\":false,\"assets\":[{\"name\":\"app.apk\","
                + "\"browser_download_url\":\"https://github.com/chenwei666/NetSerial-AI/releases/download/v0.6.0/app.apk\"}]}";
        ReleaseInfo release = new GitHubReleaseParser().parse(json);
        assertEquals("v0.6.0", release.getTagName());
        assertTrue(release.isNewerThan("0.5.0"));
        assertTrue(release.getApkUrl().endsWith("app.apk"));
    }
}
