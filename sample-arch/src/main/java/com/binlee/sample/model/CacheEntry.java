package com.binlee.sample.model;

/**
 * Created on 21-2-26.
 *
 * @author binlee sleticalboy@gmail.com
 */
@Table(name = "cache_list")
public final class CacheEntry {

  @Table.Column(name = "_mac", type = "TEXT", unique = true)
  public String mac;
  @Table.Column(name = "_pipe", type = "INTEGER")
  public int pipe;
  @Table.Column(name = "_channels", type = "TEXT")
  public String channels;
}
