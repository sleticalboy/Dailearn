package com.example.freevideo;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.binlee.sqlite.orm.Db;
import com.example.freevideo.db.SourceConverter;
import com.example.freevideo.db.StateConverter;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
@Db.Table(value = "videos")
public final class VideoItem implements Parcelable {

  // 数据库字段：id、title、tags、cover、bgm、url、date、share_key、state、reason

  /** 标题 */
  @Db.Column(value = "_title")
  public String title;
  /** 标签 */
  @Db.Column(value = "_tags")
  public String tags;
  /** 封面图片链接 */
  @Db.Column(value = "_cover")
  public String coverUrl;
  /** 视频链接 */
  @Db.Column(value = "_url")
  public String url;
  /** 背景音乐链接 */
  @Db.Column(value = "_bgm")
  public String bgmUrl;
  /** 分享链接，作为 key */
  @Db.Column(value = "_share_key", unique = true)
  public String shareUrl;
  /** 下载状态状态 */
  @Db.Column(value = "_state", type = int.class, converter = StateConverter.class)
  public DyState state = DyState.NONE;
  /** 系统下载器生成的唯一标识 */
  @Db.Column(value = "_id")
  public long id;
  /** 日期 */
  @Db.Column(value = "_date")
  public long date = System.currentTimeMillis();
  /** 失败原因 */
  @Db.Column(value = "_reason")
  public String reason;
  @Db.Column(value = "_source", type = int.class, converter = SourceConverter.class)
  public DySource source;

  public VideoItem() {
  }

  @NonNull @Override public String toString() {
    return "VideoItem{" +
      "title='" + title + '\'' +
      ", tags='" + tags + '\'' +
      ", state='" + state + '\'' +
      '}';
  }

  private VideoItem(Parcel in) {
    title = in.readString();
    tags = in.readString();
    coverUrl = in.readString();
    url = in.readString();
    bgmUrl = in.readString();
    shareUrl = in.readString();
    id = in.readLong();
    date = in.readLong();
    reason = in.readString();
  }

  public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
    @Override
    public VideoItem createFromParcel(Parcel in) {
      return new VideoItem(in);
    }

    @Override
    public VideoItem[] newArray(int size) {
      return new VideoItem[size];
    }
  };

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(tags);
    dest.writeString(coverUrl);
    dest.writeString(url);
    dest.writeString(bgmUrl);
    dest.writeString(shareUrl);
    dest.writeLong(id);
    dest.writeLong(date);
    dest.writeString(reason);
  }
}
