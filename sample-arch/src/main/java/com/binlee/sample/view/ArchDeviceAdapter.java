package com.binlee.sample.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.binlee.sample.R;
import com.binlee.sample.model.ArchDevice;
import java.util.List;

public class ArchDeviceAdapter extends RecyclerView.Adapter<ArchDeviceAdapter.ViewHolder> {

  private final List<ArchDevice> mValues;

  public ArchDeviceAdapter(List<ArchDevice> items) {
    mValues = items;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.fragment_list, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mIdView.setText(mValues.get(position).getId());
    holder.mContentView.setText(mValues.get(position).getName());
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public static final class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;
    public ArchDevice mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = (TextView) view.findViewById(R.id.item_number);
      mContentView = (TextView) view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}