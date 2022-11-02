package com.example.dyvd;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dyvd.databinding.LayoutVideoItemBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2022/10/25
 *
 * @author binlee
 */
public final class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {

  private final List<VideoItem> mItems;
  private final Callback mCallback;

  public VideoAdapter(Callback callback) {
    mItems = new ArrayList<>();
    mCallback = callback;
  }

  @NonNull @Override public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new VideoHolder(LayoutVideoItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override public void onBindViewHolder(@NonNull VideoHolder holder, int position) {
    holder.bindView(mItems.get(position), mCallback);
  }

  @Override public int getItemCount() {
    return mItems.size();
  }

  public void replace(List<VideoItem> items) {
    mItems.clear();
    if (items != null) {
      mItems.addAll(items);
      notifyItemRangeChanged(0, items.size());
    }
  }

  public void append(List<VideoItem> items) {
    if (items != null) {
      final int size = mItems.size();
      mItems.addAll(items);
      notifyItemRangeChanged(size, items.size());
    }
  }

  public void insertVideo(VideoItem item) {
    mItems.add(0, item);
    notifyItemInserted(0);
  }

  public void remove(VideoItem item) {
    final int index = mItems.indexOf(item);
    if (index >= 0) {
      mItems.remove(index);
      notifyItemRemoved(index);
    }
  }

  public void notifyItemChanged(VideoItem item) {
    final int index = mItems.indexOf(item);
    if (index >= 0) notifyItemChanged(index);
  }

  public interface Callback {

    /** 点击封面，查看全图 */
    void onClickCover(VideoItem item);

    /** 点击状态 */
    void onClickState(VideoItem item);

    /** 长按回调 */
    boolean onLongClick(VideoItem item);
  }

  static class VideoHolder extends RecyclerView.ViewHolder {

    @NonNull private final LayoutVideoItemBinding mBinding;

    private VideoHolder(@NonNull LayoutVideoItemBinding binding) {
      super(binding.getRoot());
      mBinding = binding;
    }

    public void bindView(VideoItem item, Callback callback) {
      mBinding.tvTitle.setText(item.title);
      mBinding.tvVideoTags.setText(item.tags);
      mBinding.tvState.setText(translateState(item.state));
      Glide.with(itemView.getContext()).load(item.coverUrl).into(mBinding.ivCover);
      mBinding.ivCover.setOnClickListener(v -> {
        if (callback != null) callback.onClickCover(item);
      });
      mBinding.tvState.setOnClickListener(v -> {
        if (callback != null) callback.onClickState(item);
      });
      mBinding.tvSource.setText(itemView.getContext().getString(R.string.source_from, "抖音"));

      itemView.setOnLongClickListener(v -> {
        return callback != null && callback.onLongClick(item);
      });
    }

    private int translateState(DyState state) {
      if (state == DyState.DOWNLOADED) return R.string.state_downloaded;
      if (state == DyState.DOWNLOADING) return R.string.state_downloading;
      if (state == DyState.BROKEN) return R.string.state_broken;
      return R.string.state_none;
    }
  }
}
