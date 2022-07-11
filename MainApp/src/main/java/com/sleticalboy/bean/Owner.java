package com.sleticalboy.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 21-3-19.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class Owner {
  /**
   * login : sleticalboy
   * id : 21123303
   * node_id : MDQ6VXNlcjIxMTIzMzAz
   * avatar_url : https://avatars.githubusercontent.com/u/21123303?v=4
   * gravatar_id :
   * url : https://api.github.com/users/sleticalboy
   * html_url : https://github.com/sleticalboy
   * followers_url : https://api.github.com/users/sleticalboy/followers
   * following_url : https://api.github.com/users/sleticalboy/following{/other_user}
   * gists_url : https://api.github.com/users/sleticalboy/gists{/gist_id}
   * starred_url : https://api.github.com/users/sleticalboy/starred{/owner}{/repo}
   * subscriptions_url : https://api.github.com/users/sleticalboy/subscriptions
   * organizations_url : https://api.github.com/users/sleticalboy/orgs
   * repos_url : https://api.github.com/users/sleticalboy/repos
   * events_url : https://api.github.com/users/sleticalboy/events{/privacy}
   * received_events_url : https://api.github.com/users/sleticalboy/received_events
   * type : User
   * site_admin : false
   */

  @SerializedName("login")
  public String login;
  @SerializedName("id")
  public int id;
  @SerializedName("node_id")
  public String nodeId;
  @SerializedName("avatar_url")
  public String avatarUrl;
  @SerializedName("gravatar_id")
  public String gravatarId;
  @SerializedName("url")
  public String url;
  @SerializedName("html_url")
  public String htmlUrl;
  @SerializedName("followers_url")
  public String followersUrl;
  @SerializedName("following_url")
  public String followingUrl;
  @SerializedName("gists_url")
  public String gistsUrl;
  @SerializedName("starred_url")
  public String starredUrl;
  @SerializedName("subscriptions_url")
  public String subscriptionsUrl;
  @SerializedName("organizations_url")
  public String organizationsUrl;
  @SerializedName("repos_url")
  public String reposUrl;
  @SerializedName("events_url")
  public String eventsUrl;
  @SerializedName("received_events_url")
  public String receivedEventsUrl;
  @SerializedName("type")
  public String type;
  @SerializedName("site_admin")
  public boolean siteAdmin;
}
