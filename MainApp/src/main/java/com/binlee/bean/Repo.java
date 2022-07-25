package com.binlee.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 21-3-19.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class Repo {

  /**
   * id : 186129104
   * node_id : MDEwOlJlcG9zaXRvcnkxODYxMjkxMDQ=
   * name : AutoTrack
   * full_name : sleticalboy/AutoTrack
   * private : false
   * owner : {"login":"sleticalboy","id":21123303,"node_id":"MDQ6VXNlcjIxMTIzMzAz","avatar_url":"https://avatars.githubusercontent.com/u/21123303?v=4","gravatar_id":"","url":"https://api.github.com/users/sleticalboy","html_url":"https://github.com/sleticalboy","followers_url":"https://api.github.com/users/sleticalboy/followers","following_url":"https://api.github.com/users/sleticalboy/following{/other_user}","gists_url":"https://api.github.com/users/sleticalboy/gists{/gist_id}","starred_url":"https://api.github.com/users/sleticalboy/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/sleticalboy/subscriptions","organizations_url":"https://api.github.com/users/sleticalboy/orgs","repos_url":"https://api.github.com/users/sleticalboy/repos","events_url":"https://api.github.com/users/sleticalboy/events{/privacy}","received_events_url":"https://api.github.com/users/sleticalboy/received_events","type":"User","site_admin":false}
   * html_url : https://github.com/sleticalboy/AutoTrack
   * description : 学习笔记:《Android 全埋点解决方案》
   * fork : false
   * url : https://api.github.com/repos/sleticalboy/AutoTrack
   * forks_url : https://api.github.com/repos/sleticalboy/AutoTrack/forks
   * keys_url : https://api.github.com/repos/sleticalboy/AutoTrack/keys{/key_id}
   * collaborators_url : https://api.github.com/repos/sleticalboy/AutoTrack/collaborators{/collaborator}
   * teams_url : https://api.github.com/repos/sleticalboy/AutoTrack/teams
   * hooks_url : https://api.github.com/repos/sleticalboy/AutoTrack/hooks
   * issue_events_url : https://api.github.com/repos/sleticalboy/AutoTrack/issues/events{/number}
   * events_url : https://api.github.com/repos/sleticalboy/AutoTrack/events
   * assignees_url : https://api.github.com/repos/sleticalboy/AutoTrack/assignees{/user}
   * branches_url : https://api.github.com/repos/sleticalboy/AutoTrack/branches{/branch}
   * tags_url : https://api.github.com/repos/sleticalboy/AutoTrack/tags
   * blobs_url : https://api.github.com/repos/sleticalboy/AutoTrack/git/blobs{/sha}
   * git_tags_url : https://api.github.com/repos/sleticalboy/AutoTrack/git/tags{/sha}
   * git_refs_url : https://api.github.com/repos/sleticalboy/AutoTrack/git/refs{/sha}
   * trees_url : https://api.github.com/repos/sleticalboy/AutoTrack/git/trees{/sha}
   * statuses_url : https://api.github.com/repos/sleticalboy/AutoTrack/statuses/{sha}
   * languages_url : https://api.github.com/repos/sleticalboy/AutoTrack/languages
   * stargazers_url : https://api.github.com/repos/sleticalboy/AutoTrack/stargazers
   * contributors_url : https://api.github.com/repos/sleticalboy/AutoTrack/contributors
   * subscribers_url : https://api.github.com/repos/sleticalboy/AutoTrack/subscribers
   * subscription_url : https://api.github.com/repos/sleticalboy/AutoTrack/subscription
   * commits_url : https://api.github.com/repos/sleticalboy/AutoTrack/commits{/sha}
   * git_commits_url : https://api.github.com/repos/sleticalboy/AutoTrack/git/commits{/sha}
   * comments_url : https://api.github.com/repos/sleticalboy/AutoTrack/comments{/number}
   * issue_comment_url : https://api.github.com/repos/sleticalboy/AutoTrack/issues/comments{/number}
   * contents_url : https://api.github.com/repos/sleticalboy/AutoTrack/contents/{+path}
   * compare_url : https://api.github.com/repos/sleticalboy/AutoTrack/compare/{base}...{head}
   * merges_url : https://api.github.com/repos/sleticalboy/AutoTrack/merges
   * archive_url : https://api.github.com/repos/sleticalboy/AutoTrack/{archive_format}{/ref}
   * downloads_url : https://api.github.com/repos/sleticalboy/AutoTrack/downloads
   * issues_url : https://api.github.com/repos/sleticalboy/AutoTrack/issues{/number}
   * pulls_url : https://api.github.com/repos/sleticalboy/AutoTrack/pulls{/number}
   * milestones_url : https://api.github.com/repos/sleticalboy/AutoTrack/milestones{/number}
   * notifications_url : https://api.github.com/repos/sleticalboy/AutoTrack/notifications{?since,all,participating}
   * labels_url : https://api.github.com/repos/sleticalboy/AutoTrack/labels{/name}
   * releases_url : https://api.github.com/repos/sleticalboy/AutoTrack/releases{/id}
   * deployments_url : https://api.github.com/repos/sleticalboy/AutoTrack/deployments
   * created_at : 2019-05-11T12:17:06Z
   * updated_at : 2021-03-09T14:56:28Z
   * pushed_at : 2021-03-09T14:56:26Z
   * git_url : git://github.com/sleticalboy/AutoTrack.git
   * ssh_url : git@github.com:sleticalboy/AutoTrack.git
   * clone_url : https://github.com/sleticalboy/AutoTrack.git
   * svn_url : https://github.com/sleticalboy/AutoTrack
   * homepage :
   * size : 192
   * stargazers_count : 6
   * watchers_count : 6
   * language : Java
   * has_issues : true
   * has_projects : true
   * has_downloads : true
   * has_wiki : true
   * has_pages : false
   * forks_count : 0
   * mirror_url : null
   * archived : false
   * disabled : false
   * open_issues_count : 1
   * license : null
   * forks : 0
   * open_issues : 1
   * watchers : 6
   * default_branch : master
   */

  @SerializedName("id")
  public int id;
  @SerializedName("node_id")
  public String nodeId;
  @SerializedName("name")
  public String name;
  @SerializedName("full_name")
  public String fullName;
  @SerializedName("private")
  public boolean privateX;
  @SerializedName("owner")
  public Owner owner;
  @SerializedName("html_url")
  public String htmlUrl;
  @SerializedName("description")
  public String description;
  @SerializedName("fork")
  public boolean fork;
  @SerializedName("url")
  public String url;
  @SerializedName("forks_url")
  public String forksUrl;
  @SerializedName("keys_url")
  public String keysUrl;
  @SerializedName("collaborators_url")
  public String collaboratorsUrl;
  @SerializedName("teams_url")
  public String teamsUrl;
  @SerializedName("hooks_url")
  public String hooksUrl;
  @SerializedName("issue_events_url")
  public String issueEventsUrl;
  @SerializedName("events_url")
  public String eventsUrl;
  @SerializedName("assignees_url")
  public String assigneesUrl;
  @SerializedName("branches_url")
  public String branchesUrl;
  @SerializedName("tags_url")
  public String tagsUrl;
  @SerializedName("blobs_url")
  public String blobsUrl;
  @SerializedName("git_tags_url")
  public String gitTagsUrl;
  @SerializedName("git_refs_url")
  public String gitRefsUrl;
  @SerializedName("trees_url")
  public String treesUrl;
  @SerializedName("statuses_url")
  public String statusesUrl;
  @SerializedName("languages_url")
  public String languagesUrl;
  @SerializedName("stargazers_url")
  public String stargazersUrl;
  @SerializedName("contributors_url")
  public String contributorsUrl;
  @SerializedName("subscribers_url")
  public String subscribersUrl;
  @SerializedName("subscription_url")
  public String subscriptionUrl;
  @SerializedName("commits_url")
  public String commitsUrl;
  @SerializedName("git_commits_url")
  public String gitCommitsUrl;
  @SerializedName("comments_url")
  public String commentsUrl;
  @SerializedName("issue_comment_url")
  public String issueCommentUrl;
  @SerializedName("contents_url")
  public String contentsUrl;
  @SerializedName("compare_url")
  public String compareUrl;
  @SerializedName("merges_url")
  public String mergesUrl;
  @SerializedName("archive_url")
  public String archiveUrl;
  @SerializedName("downloads_url")
  public String downloadsUrl;
  @SerializedName("issues_url")
  public String issuesUrl;
  @SerializedName("pulls_url")
  public String pullsUrl;
  @SerializedName("milestones_url")
  public String milestonesUrl;
  @SerializedName("notifications_url")
  public String notificationsUrl;
  @SerializedName("labels_url")
  public String labelsUrl;
  @SerializedName("releases_url")
  public String releasesUrl;
  @SerializedName("deployments_url")
  public String deploymentsUrl;
  @SerializedName("created_at")
  public String createdAt;
  @SerializedName("updated_at")
  public String updatedAt;
  @SerializedName("pushed_at")
  public String pushedAt;
  @SerializedName("git_url")
  public String gitUrl;
  @SerializedName("ssh_url")
  public String sshUrl;
  @SerializedName("clone_url")
  public String cloneUrl;
  @SerializedName("svn_url")
  public String svnUrl;
  @SerializedName("homepage")
  public String homepage;
  @SerializedName("size")
  public int size;
  @SerializedName("stargazers_count")
  public int stargazersCount;
  @SerializedName("watchers_count")
  public int watchersCount;
  @SerializedName("language")
  public String language;
  @SerializedName("has_issues")
  public boolean hasIssues;
  @SerializedName("has_projects")
  public boolean hasProjects;
  @SerializedName("has_downloads")
  public boolean hasDownloads;
  @SerializedName("has_wiki")
  public boolean hasWiki;
  @SerializedName("has_pages")
  public boolean hasPages;
  @SerializedName("forks_count")
  public int forksCount;
  @SerializedName("mirror_url")
  public String mirrorUrl;
  @SerializedName("archived")
  public boolean archived;
  @SerializedName("disabled")
  public boolean disabled;
  @SerializedName("open_issues_count")
  public int openIssuesCount;
  @SerializedName("license")
  public Object license;
  @SerializedName("forks")
  public int forks;
  @SerializedName("open_issues")
  public int openIssues;
  @SerializedName("watchers")
  public int watchers;
  @SerializedName("default_branch")
  public String defaultBranch;
}
