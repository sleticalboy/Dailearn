package com.binlee.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.binlee.sample.util.Glog;
import com.binlee.sample.view.DeviceListFragment;

public final class MainActivity extends AppCompatActivity {

  private static final String TAG = Glog.wrapTag("MainActivity");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // show Fragment
    Bundle args = new Bundle();
    args.putInt(DeviceListFragment.ARG_COLUMN_COUNT, 1);
    showFragment(DeviceListFragment.class.getName(), args);
  }

  private void showFragment(String clazz, Bundle args) {
    Glog.v(TAG, "showFragment() " + clazz);
    FragmentManager mgr = getSupportFragmentManager();
    Fragment fragment = mgr.findFragmentByTag(clazz);
    FragmentTransaction transaction = mgr.beginTransaction();
    if (fragment == null) {
      fragment = mgr.getFragmentFactory().instantiate(getClassLoader(), clazz);
      fragment.setArguments(args);
      transaction.add(R.id.fl_container, fragment, clazz);
    }
    transaction.show(fragment).commitNow();
  }
}