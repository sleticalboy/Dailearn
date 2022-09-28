package com.binlee.learning;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.binlee.dl.DlManager;
import com.binlee.dl.host.proxy.ProxyActivity;
import com.binlee.dl.host.proxy.ProxyService;
import com.binlee.dl.host.util.FileUtils;
import com.binlee.dl.plugin.DlServiceRunner;
import com.binlee.learning.base.BaseActivity;
import com.binlee.learning.bean.ModuleItem;
import com.binlee.learning.databinding.ActivityListItemBinding;
import java.io.File;
import java.io.IOException;

/**
 * Created on 2022/9/26
 *
 * @author binlee
 */
public final class PluginManageActivity extends BaseActivity implements ServiceConnection {

  private static final String TAG = "PluginManageActivity";

  private static final ModuleItem[] MODULE_ITEMS = {
    // 加载插件
    new ModuleItem("加载插件", "load_plugin"),
    // 启动 activity
    new ModuleItem("启动 activity", "start_activity"),
    // 启动 service
    new ModuleItem("启动 service", "start_service"),
    // 停止 service
    new ModuleItem("停止 service", "stop_service"),
    // 绑定 service
    new ModuleItem("绑定 service", "bind_service"),
    // 解绑 service
    new ModuleItem("解绑 service", "unbind_service"),
    // 启动 receiver
    // 解绑 receiver
    // 发送广播
    new ModuleItem("发送广播", "send_broadcast"),
    // 查询 provider 数据
    // 卸载插件
    new ModuleItem("卸载插件", "unload_plugin"),
  };
  private ActivityListItemBinding mBinding;

  @NonNull @Override protected View layout() {
    mBinding = ActivityListItemBinding.inflate(getLayoutInflater());
    return mBinding.getRoot();
  }

  @Override protected void initView() {
    mBinding.recyclerView.setAdapter(new ItemAdapter(this));
  }

  @Override public void onServiceConnected(ComponentName name, IBinder service) {
    Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
  }

  @Override public void onServiceDisconnected(ComponentName name) {
    Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
  }

  private void onModuleItemClick(ModuleItem item) {
    if ("load_plugin".equals(item.getCls())) {
      loadPlugin();
    } else if ("unload_plugin".equals(item.getCls())) {
      unloadPlugin();
    } else if ("start_activity".equals(item.getCls())) {
      startPluginActivity();
    } else if ("start_service".equals(item.getCls())) {
      startPluginService();
    } else if ("stop_service".equals(item.getCls())) {
      stopPluginService();
    } else if ("bind_service".equals(item.getCls())) {
      bindPluginService();
    } else if ("unbind_service".equals(item.getCls())) {
      unbindPluginService();
    } else if ("send_broadcast".equals(item.getCls())) {
      sendPluginBroadcast();
    } else {
      if (item.getClazz() != Object.class) {
        startActivity(new Intent(this, item.getClazz()));
      } else {
        Toast.makeText(this, "未实现！", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void sendPluginBroadcast() {
    final Intent intent = new Intent("com.sample.plugin.action.SAMPLE_ACTION");
    intent.putExtra("key_1", "value_1");
    sendBroadcast(intent);
  }

  private void unbindPluginService() {
    // 插件中的 service：com.example.plugin.PluginService
    final ComponentName target = new ComponentName("com.example.plugin", "com.example.plugin.PluginService");
    DlServiceRunner.unbind(this, target, this);
    Log.d(TAG, "unbindPluginService()");
  }

  private void bindPluginService() {
    // 插件中的 service：com.example.plugin.PluginService
    final ComponentName target = new ComponentName("com.example.plugin", "com.example.plugin.PluginService");
    DlServiceRunner.bind(this, target, this);
    Log.d(TAG, "bindPluginService()");
  }

  private void stopPluginService() {
    // 插件中的 service：com.example.plugin.PluginService
    final ComponentName target = new ComponentName("com.example.plugin", "com.example.plugin.PluginService");
    DlServiceRunner.stop(this, target);
    Log.d(TAG, "stopPluginService()");
  }

  private void startPluginService() {
    // 插件中的 service：com.example.plugin.PluginService
    final ComponentName target = new ComponentName("com.example.plugin", "com.example.plugin.PluginService");
    DlServiceRunner.start(this, target);
    Log.d(TAG, "startPluginService()");
  }

  private void startPluginActivity() {
    // 插件中的 activity：com.example.plugin.PluginActivity
    ProxyActivity.start(this, new ComponentName("com.example.plugin", "com.example.plugin.PluginActivity"));
  }

  private void unloadPlugin() {
    File plugin = new File(getFilesDir(), "plugins/plugin.zip");
    DlManager.get().uninstall(plugin.getAbsolutePath());
    FileUtils.delete(plugin);
    Log.w(TAG, "unloadPlugin() finished");
  }

  private void loadPlugin() {
    File plugin = new File(getFilesDir(), "plugins/plugin.zip");
    try {
      FileUtils.copy(getAssets().open("plugin.apk"), plugin);
    } catch (IOException e) {
      Log.d(TAG, "loadPlugin() failed", e);
      return;
    }
    DlManager.get().install(plugin.getAbsolutePath());
    Toast.makeText(this, "加载插件成功", Toast.LENGTH_SHORT).show();
  }

  private static class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

    private final PluginManageActivity mHost;

    private ItemAdapter(PluginManageActivity host) {
      mHost = host;
    }

    @NonNull @Override public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      final TextView itemView = new TextView(mHost);
      itemView.setBackgroundResource(R.drawable.module_item_bg);
      return new ItemHolder(itemView);
    }

    @Override public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
      final ModuleItem item = MODULE_ITEMS[position];
      holder.mTextView.setText(item.getTitle());
      holder.itemView.setOnClickListener(v -> mHost.onModuleItemClick(item));
    }

    @Override public int getItemCount() {
      return MODULE_ITEMS.length;
    }
  }

  private static class ItemHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    public ItemHolder(@NonNull View itemView) {
      super(itemView);
      mTextView = (TextView) itemView;
      mTextView.setGravity(Gravity.CENTER);
      mTextView.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      );

      mTextView.setPadding(32, 16, 32, 16);
      mTextView.setTextSize(24F);
      mTextView.setTextColor(Color.BLUE);
    }
  }
}
