package com.binlee.learning.bt.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created on 20-9-23.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class SppClient {

  private static final String TAG = "SppClient";

  private final BluetoothDevice mDevice;
  private final Context mContext;
  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final ParcelUuid uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);
    }
  };

  public SppClient(Context context, String address) {
    mContext = context.getApplicationContext();
    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    context.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
  }

  public void connect(ParcelUuid uuid) {
    if (uuid == null) {
      final ParcelUuid[] uuids = mDevice.getUuids();
      if (uuids != null && uuids.length > 1) {
        uuid = uuids[0];
      }
      if (uuid == null) {
        final boolean fetched = mDevice.fetchUuidsWithSdp();
        Log.d(TAG, "fetched: " + fetched);
      } else {
        connect(uuid);
      }
    } else {
      // https://www.runoob.com/sql/sql-tutorial.html
      try {
        new DataReceiver(mDevice.createInsecureRfcommSocketToServiceRecord(uuid.getUuid()))
          .start(data -> {
            //
          });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static class DataReceiver extends Thread {

    private final BluetoothSocket mSocket;
    private final InputStream mIs;
    private final OutputStream mOs;
    private OnDataReceivedListener mListener;

    private DataReceiver(final BluetoothSocket socket) {
      InputStream is;
      OutputStream os;
      try {
        is = socket.getInputStream();
        os = socket.getOutputStream();
      } catch (IOException e) {
        Log.e(TAG, "init DataReceiver error", e);
        is = null;
        os = null;
      }
      mIs = is;
      mOs = os;
      mSocket = socket;
    }

    public synchronized void start(OnDataReceivedListener listener) {
      mListener = listener;
      start();
    }

    @Override
    public void run() {
      if (mIs == null) {
        return;
      }
      if (mOs == null) {
        return;
      }
      final byte[] buf = new byte[1024];
      while (true) {
        try {
          final int read = mIs.read(buf, 0, buf.length);
          if (read < buf.length) {
            final byte[] data = new byte[read];
            System.arraycopy(buf, 0, data, 0, read);
            Arrays.fill(buf, 0, read - 1, (byte) 0);
            onDataReceived(data);
          }
        } catch (IOException e) {
          Log.e(TAG, "DataReceiver#run() error", e);
          break;
        }
      }
      try {
        mSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void onDataReceived(byte[] data) {
      if (mListener != null) {
        mListener.onDataReceived(data);
      }
    }
  }

  public interface OnDataReceivedListener {

    void onDataReceived(byte[] data);
  }
}
