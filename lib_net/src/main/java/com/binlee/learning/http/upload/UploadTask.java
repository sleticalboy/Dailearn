package com.binlee.learning.http.upload;

import com.binlee.learning.http.Task;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public final class UploadTask extends Task {

  public UploadTask() {
    super("minxing %s", "UploadTask");
  }

  @Override
  public void execute() {

  }

  @Override
  public void cancel() {

  }

  @Override
  public boolean isCanceled() {
    return false;
  }
}
