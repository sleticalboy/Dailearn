package com.example.dyvd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dyvd.databinding.LayoutVideoItemBinding;
import java.util.List;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {

  private final Context mContext;
  private final List<VideoItem> mItems;

  private Callback mCallback;

  public VideoAdapter(Context context, List<VideoItem> items) {
    mContext = context;
    mItems = items;
  }

  public void setCallback(Callback callback) {
    mCallback = callback;
  }

  @NonNull @Override public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new VideoHolder(LayoutVideoItemBinding.inflate(LayoutInflater.from(mContext), parent, false));
  }

  @Override public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
    holder.bindView(mItems.get(position), mCallback);
  }

  @Override public int getItemCount() {
    return mItems.size();
  }

  public void insertVideo(VideoItem item) {
    mItems.add(0, item);
    notifyItemInserted(0);
  }

  public interface Callback {

    /**
     * 点击封面，查看全图
     *
     * @param coverUrl 封面 url
     */
    void onCoverClick(String coverUrl);

    /**
     * 点击状态
     *
     * @param state 状态
     * @param url 视频链接
     */
    void onStateClick(DyState state, String url);
  }

  private static String getState(DyState state) {
    if (state == DyState.DOWNLOADED) return "已下载";
    if (state == DyState.DOWNLOADING) return "正在下载";
    if (state == DyState.BROKEN) return "无法下载";
    return "未下载";
  }

  static class VideoHolder extends RecyclerView.ViewHolder {

    @NonNull
    private final LayoutVideoItemBinding mBinding;

    public VideoHolder(@NonNull LayoutVideoItemBinding binding) {
      super(binding.getRoot());
      mBinding = binding;
    }

    public void bindView(VideoItem item, Callback callback) {
      mBinding.tvTitle.setText(item.title);
      mBinding.tvState.setText(getState(item.state));
      Glide.with(itemView.getContext()).load(item.coverUrl).into(mBinding.ivCover);
      mBinding.ivCover.setOnClickListener(v -> {
        if (callback != null) callback.onCoverClick(item.coverUrl);
      });
      mBinding.tvState.setOnClickListener(v -> {
        if (callback != null) callback.onStateClick(item.state, item.url);
      });
      mBinding.tvSource.setText(itemView.getContext().getString(R.string.source_from, "抖音"));
    }
  }
}
