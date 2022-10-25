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

  public VideoAdapter(Context context, List<VideoItem> items) {
    mContext = context;
    mItems = items;
  }

  @NonNull @Override public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new VideoHolder(LayoutVideoItemBinding.inflate(LayoutInflater.from(mContext), parent, false));
  }

  @Override public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
    holder.bindView(mItems.get(position));
  }

  @Override public int getItemCount() {
    return mItems.size();
  }

  public void insertVideo(VideoItem item) {
    mItems.add(0, item);
    notifyItemInserted(0);
  }

  private static String getState(DyState state) {
    if (state == DyState.DOWNLOADED) return "已下载";
    if (state == DyState.DOWNLOADING) return "正在下载";
    return "未下载";
  }

  static class VideoHolder extends RecyclerView.ViewHolder {


    @NonNull
    private final LayoutVideoItemBinding mBinding;

    public VideoHolder(@NonNull LayoutVideoItemBinding binding) {
      super(binding.getRoot());
      mBinding = binding;
    }

    public void bindView(VideoItem item) {
      mBinding.tvTitle.setText(item.title);
      mBinding.tvState.setText(getState(item.state));
      Glide.with(itemView.getContext()).load(item.coverUrl).into(mBinding.ivCover);
    }
  }
}
