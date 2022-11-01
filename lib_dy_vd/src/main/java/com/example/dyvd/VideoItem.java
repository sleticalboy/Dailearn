package com.example.dyvd;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.example.dyvd.db.Db;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
@Db.Table(name = "videos")
public final class VideoItem implements Parcelable {

  // 数据库字段：id、title、tags、cover、bgm、url、date、share_key、state、reason

  /** 标题 */
  @Db.Column(name = "_title", type = String.class)
  public String title;
  /** 标签 */
  @Db.Column(name = "_tags", type = String.class)
  public String tags;
  /** 封面图片链接 */
  @Db.Column(name = "_cover", type = String.class)
  public String coverUrl;
  /** 视频链接 */
  @Db.Column(name = "_url", type = String.class)
  public String url;
  /** 背景音乐链接 */
  @Db.Column(name = "_bgm", type = String.class)
  public String bgmUrl;
  /** 分享链接，作为 key */
  @Db.Column(name = "_share_key", type = String.class)
  public String shareUrl;
  /** 下载状态状态 */
  @Db.Column(name = "_state", type = int.class)
  public DyState state = DyState.NONE;
  /** 系统下载器生成的唯一标识 */
  @Db.Column(name = "_id", type = long.class)
  public long id;
  /** 日期 */
  @Db.Column(name = "_date", type = long.class)
  public long date = System.currentTimeMillis();
  /** 失败原因 */
  @Db.Column(name = "_reason", type = String.class)
  public String reason;

  public VideoItem() {}

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
