package com.example.fileserver.core;

import android.net.Uri;
import android.os.Environment;
import com.example.fileserver.FileUtil;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.RequestBody;
import com.yanzhenjie.andserver.http.ResponseBody;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2022/11/17
 *
 * @author binlee
 */
@RestController
public class FilesController {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd aHH:mm:ss");
  private static final DecimalFormat FILE_SIZE_FORMAT = new DecimalFormat("#,##0.##");
  private static final String[] SIZE_UNITS = new String[] { "b", "kb", "M", "G", "T" };

  @GetMapping(path = "/file/list")
  public JsonBody listFiles(@RequestParam(name = "rootPath", required = false) String rootPath) throws JSONException {
    File root;
    if (rootPath == null || rootPath.trim().length() == 0) {
      // 根目录
      root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    } else {
      root = new File(rootPath);
    }

    if (!root.isDirectory()) return new JsonBody(FileUtil.success(new JSONArray()));

    final File[] list = root.listFiles();
    if (list == null || list.length == 0) return new JsonBody(FileUtil.success(new JSONArray()));

    // obj.name, obj.url, obj.isDir, obj.size, obj.sizeString, obj.dateModified, obj.dateModifiedString

    final JSONArray data = new JSONArray();
    for (File file : list) {
      final JSONObject json = new JSONObject();
      json.put("name", file.getName());
      json.put("url", file.getAbsolutePath());
      if (file.isDirectory()) {
        json.put("isDir", 1);
      } else {
        json.put("isDir", 0);
        json.put("size", file.length());
        json.put("sizeString", formattedSize(file.length()));
      }
      json.put("dateModified", file.lastModified());
      json.put("dateModifiedString", formattedDate(file.lastModified()));
      data.put(json);
    }

    return new JsonBody(FileUtil.success(data));
  }

  @PostMapping(path = "/file/download")
  public ResponseBody download(HttpResponse response, @RequestParam(name = "rootPath", required = false) String path)
    throws JSONException {
    if (path == null || path.trim().length() == 0) {
      return new JsonBody(FileUtil.error("empty file path"));
    }
    try {
      final File file = new File(path);
      if (file.isFile() && file.exists()) {

        final FileBody fileBody = new FileBody(file);
        response.setStatus(200);
        response.setHeader("Accept-Ranges", "bytes");
        // 防止浏览器下载时文件名乱码
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + Uri.encode(file.getName()));
        return fileBody;
      }
      return new JsonBody(FileUtil.error("file not exist"));
    } catch (Throwable tr) {
      return new JsonBody(FileUtil.error(tr.getClass() + ":" + tr.getMessage()));
    }
  }

  // @PostMapping(path = "file/upload")
  // public JsonBody upload(RequestBody request) throws JSONException {
  //   return new JsonBody(FileUtil.error("unsupported"));
  // }

  private static String formattedDate(long timeInMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeInMillis);
    Date date = calendar.getTime();
    return DATE_FORMAT.format(date);
  }

  private static String formattedSize(long size) {
    if (size <= 0) return "0";
    // 计算单位的，原理是利用lg,公式是 lg(1024^n) = nlg(1024)，最后 nlg(1024)/lg(1024) = n。
    int index = (int) (Math.log10(size) / Math.log10(1024));
    // 计算原理是，size/单位值。单位值指的是:比如说b = 1024,KB = 1024^2
    return FILE_SIZE_FORMAT.format(size / Math.pow(1024, index)) + " " + SIZE_UNITS[index];
  }
}
