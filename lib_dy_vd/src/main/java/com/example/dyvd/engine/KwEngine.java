package com.example.dyvd.engine;

import android.net.Uri;
import android.util.Log;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created on 2022/11/3
 *
 * @author binlee
 */
public class KwEngine extends Engine {

  public static final String DOMAIN = "v.kuaishou.com";
  private static final String POST_DATA = "{\"operationName\":\"visionShortVideoReco\",\"variables\":{\"utmSource\":\"app_share\",\"utmMedium\":\"app_share\",\"page\":\"detail\",\"photoId\":\"%s\",\"utmCampaign\":\"app_share\"},\"query\":\"fragment photoContent on PhotoEntity {\\nid\\nduration\\ncaption\\noriginCaption\\nlikeCount\\nviewCount\\nrealLikeCount\\ncoverUrl\\nphotoUrl\\nphotoH265Url\\nmanifest\\nmanifestH265\\nvideoResource\\ncoverUrls {\\nurl\\n__typename\\n}\\ntimestamp\\nexpTag\\nanimatedCoverUrl\\ndistance\\nvideoRatio\\nliked\\nstereoType\\nprofileUserTopPhoto\\nmusicBlocked\\n__typename\\n}\\n\\nquery visionShortVideoReco($semKeyword: String, $semCrowd: String, $utmSource: String, $utmMedium: String, $page: String, $photoId: String, $utmCampaign: String) {\\nvisionShortVideoReco(semKeyword: $semKeyword, semCrowd: $semCrowd, utmSource: $utmSource, utmMedium: $utmMedium, page: $page, photoId: $photoId, utmCampaign: $utmCampaign) {\\nllsid\\nfeeds {\\ntype\\nauthor {\\nid\\nname\\nfollowing\\nheaderUrl\\n__typename\\n}\\nphoto {\\n...photoContent\\n__typename\\n}\\ntags {\\ntype\\nname\\n__typename\\n}\\ncanAddComment\\n__typename\\n}\\n__typename\\n}\\n}\\n\"}";
  private static final String TOOL_URL = "https://www.dy114.com/jiexi/videoParse_uniform.php";
  private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36";

  // 去水印原理：
  // https://www.likecs.com/show-204863386.html
  // https://blog.csdn.net/m0_50944918/article/details/110875995

  // 1、请求短链，响应头获取重定向地址 https://v.m.chenzhongtech.com/fw/photo/3x6zgb6gcnkcrd9?fid=0&...
  // 2、截取 3x 到 ? 之间内容 {photo_id}；
  // 3、post https://www.kuaishou.com/graphql String.format(POST_DATA, photo_id) 获取响应 json
  // 4、从 json 中获取视频 url： ['data']['visionVideoDetail']['photo']['photoUrl']
  public KwEngine(String shareUrl) {
    super(shareUrl);
  }

  // https://v.kuaishou.com/BgUhrh 今日播下善良的种子，明日必能开出幸运之花！ "天生我有戏 "剧星计划 "快手弹幕 该作品在快手被播放过223.9万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/AWFZfn "健身增肌减脂塑型 "健身健美 "健身 爱生活 爱健美 打开头像看现场精彩直播 该作品在快手被播放过41.8万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/wQG7ly 小李这一手把大哥差点气哭 该作品在快手被播放过504.7万次，点击链接，打开【快手】直接观看！
  // https://v.kuaishou.com/AkiUFc 如果你有特异功能，你会怎么帮他们"随机舞蹈 "快手舞蹈  该作品在快手被播放过180.4万次，点击链接，打开【快手】直接观看！
  @Override protected String parseShareUrl(String text) {
    if (text == null || !text.contains(DOMAIN)) return null;

    int start = text.indexOf("http");
    if (start < 0) return null;
    final int end = text.indexOf(' ');
    return end < 0 ? text.substring(start) : text.substring(start, end);
  }

  @Override protected VideoItem fromJson(String shareUrl, String text) {
    return null;
  }

  @Override protected String getVideoInfo() throws IOException, JSONException {
    // curl 'https://www.dy114.com/jiexi/videoParse_uniform.php' \
    //   -H 'authority: www.dy114.com' \
    //   -H 'accept: text/html, */*; q=0.01' \
    //   -H 'accept-language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7' \
    //   -H 'content-type: application/x-www-form-urlencoded; charset=UTF-8' \
    //   -H 'cookie: PHPSESSID=j5k549bn9skjdrbov8llhfj78k; Hm_lvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629702; Hm_lpvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629712' \
    //   -H 'origin: https://www.dy114.com' \
    //   -H 'sec-ch-ua: "Google Chrome";v="107", "Chromium";v="107", "Not=A?Brand";v="24"' \
    //   -H 'sec-ch-ua-mobile: ?0' \
    //   -H 'sec-ch-ua-platform: "Linux"' \
    //   -H 'sec-fetch-dest: empty' \
    //   -H 'sec-fetch-mode: cors' \
    //   -H 'sec-fetch-site: same-origin' \
    //   -H 'user-agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36' \
    //   -H 'x-requested-with: XMLHttpRequest' \
    //   --data-raw 'url=https%3A%2F%2Fv.kuaishou.com%2FAkiUFc' \
    //   --compressed ;

    final String cmd = "curl 'https://www.dy114.com/jiexi/videoParse_uniform.php' \\" +
            "  -H 'authority: www.dy114.com' \\" +
            "  -H 'accept: text/html, */*; q=0.01' \\" +
            "  -H 'accept-language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7' \\" +
            "  -H 'content-type: application/x-www-form-urlencoded; charset=UTF-8' \\" +
            "  -H 'cookie: PHPSESSID=j5k549bn9skjdrbov8llhfj78k; Hm_lvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629702; Hm_lpvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629712' \\" +
            "  -H 'origin: https://www.dy114.com' \\" +
            "  -H 'sec-ch-ua: \"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"' \\" +
            "  -H 'sec-ch-ua-mobile: ?0' \\" +
            "  -H 'sec-ch-ua-platform: \"Linux\"' \\" +
            "  -H 'sec-fetch-dest: empty' \\" +
            "  -H 'sec-fetch-mode: cors' \\" +
            "  -H 'sec-fetch-site: same-origin' \\" +
            "  -H 'user-agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36' \\" +
            "  -H 'x-requested-with: XMLHttpRequest' \\" +
            "  --data-raw 'url=%s' \\" +
            "  --compressed";

    // final Process proc = Runtime.getRuntime().exec(String.format(cmd, Uri.encode(shortUrl)));
    // String result;
    // if (proc.exitValue() == 0) {
    //   result = DyUtil.streamAsString(proc.getInputStream(), null);
    // } else {
    //   result = DyUtil.streamAsString(proc.getErrorStream(), null);
    // }

    Map<String, String> headers = new HashMap<>();
    headers.put("user-agent", USER_AGENT);
    headers.put("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
    headers.put("cookie", "PHPSESSID=j5k549bn9skjdrbov8llhfj78k; Hm_lvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629702; Hm_lpvt_b5ae0a50c43eeaa3321c86b7607d82e8=1667629712");
    headers.put("origin", "https://www.dy114.com");
    headers.put("sec-ch-ua", "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
    headers.put("sec-ch-ua-mobile", "?0");
    headers.put("sec-ch-ua-platform", "Linux");
    headers.put("sec-fetch-dest", "empty");
    headers.put("sec-fetch-mode", "cors");
    headers.put("sec-fetch-site", "same-origin");
    headers.put("x-requested-with", "XMLHttpRequest");

    HttpURLConnection conn = (HttpURLConnection) new URL(TOOL_URL).openConnection();
    conn.setConnectTimeout(3000);
    conn.setReadTimeout(3000);
    conn.setRequestMethod("POST");
    for (String key : headers.keySet()) {
      conn.addRequestProperty(key, headers.get(key));
    }
    conn.setDoOutput(true);
    conn.getOutputStream().write(("url=" + shortUrl).getBytes(StandardCharsets.UTF_8));

    final int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
            + "], mime: " + conn.getContentType());

    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream(), conn.getContentEncoding());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream(), conn.getContentEncoding());
    }
    // Split by line, then ensure each line can fit into Log's maximum length.
    for (int i = 0, length = result.length(); i < length; i++) {
      int newline = result.indexOf('\n', i);
      newline = newline != -1 ? newline : length;
      do {
        int limit = Math.min(newline, i + MAX_LOG_LENGTH);
        Log.d(TAG, "getVideoInfo() " + result.substring(i, limit));
        i = limit;
      } while (i < newline);
    }

    final Document document = Jsoup.parse(result);
    Log.d(TAG, "getVideoInfo() html: " + document);
    return null;
  }

  protected String getVideoInfo0() throws IOException, JSONException {
    // did=web_faf8756a20cc4724bc0ddbe676ebfcf7; didv=1667461246000; kpf=PC_WEB; kpn=KUAISHOU_VISION; clientid=3
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("Connection", "keep-alive");
    headers.put("Cookie", "did=web_faf8756a20cc4724bc0ddbe676ebfcf7; didv=1667461246000");
    headers.put("Host", "v.kuaishou.com");
    headers.put("User-Agent", USER_AGENT);

    HttpURLConnection conn = (HttpURLConnection) new URL(shortUrl).openConnection();
    conn.setRequestMethod("GET");
    for (String key : headers.keySet()) {
      conn.addRequestProperty(key, headers.get(key));
    }
    final int code = conn.getResponseCode();
    Log.d(TAG, "getVideoInfo() http code: " + code + ", msg: [" + conn.getResponseMessage()
      + "], mime: " + conn.getContentType());

    String location = conn.getHeaderField("Location");
    if (location == null) {
      location = conn.toString();
      location = location.substring(location.indexOf(':') + 1);
    }
    // skip for long video
    if (location.contains("long-video")) return null;

    Log.d(TAG, "getVideoInfo() location: " + location);

    final int end = location.substring(0, location.indexOf('?')).lastIndexOf('/');
    String referer = "https://www.kuaishou.com/short-video/" + location.substring(end + 1);
    Log.d(TAG, "getVideoInfo() referer: " + referer);

    tryNext(location);
    // return tryFinal(String.format(POST_DATA, getPhotoId(referer)), referer);
    return null;
  }

  private String getPhotoId(String location) {
    int start, end;
    if ((start = location.indexOf("photoId=")) > 0) {
      start += 8;
      end = location.indexOf('&', start);
    } else {
      end = location.indexOf('?');
      start = location.substring(0, end).lastIndexOf('/');
    }

    final String photoId = location.substring(start + 1, end);
    Log.d(TAG, "getPhotoId() photoId: " + photoId);
    return photoId;
  }

  private void tryNext(String location) throws IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("Connection", "keep-alive");
    headers.put("Cache-Control", "max-age=0");
    headers.put("Cookie", "did=web_fb03fe0b03e54f3488eaa1141f80da2e; didv=1667484473000; kpf=PC_WEB; kpn=KUAISHOU_VISION; clientid=3; client_key=65890b29");
    headers.put("Host", "v.kuaishou.com");
    headers.put("User-Agent", USER_AGENT);

    HttpURLConnection conn = (HttpURLConnection) new URL(location).openConnection();
    conn.setRequestMethod("GET");
    for (final String key : headers.keySet()) {
      conn.addRequestProperty(key, headers.get(key));
    }
    final int code = conn.getResponseCode();
    Log.d(TAG, "tryNext() " + conn.getHeaderField(null) + ", mime: " + conn.getContentType());

    dumpHeaders(conn, "next step");

    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream(), conn.getContentEncoding());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream(), conn.getContentEncoding());
    }
    // Split by line, then ensure each line can fit into Log's maximum length.
    for (int i = 0, length = result.length(); i < length; i++) {
      int newline = result.indexOf('\n', i);
      newline = newline != -1 ? newline : length;
      do {
        int limit = Math.min(newline, i + MAX_LOG_LENGTH);
        Log.d(TAG, "tryNext() " + result.substring(i, limit));
        i = limit;
      } while (i < newline);
    }
  }

  private String tryFinal(String data, String referer) throws IOException, JSONException {
    // Log.d(TAG, "tryFinal() " + data);

    Map<String, String> headers = new HashMap<>();
    headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
    headers.put("Accept-Encoding", "gzip, deflate, br");
    headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
    headers.put("Connection", "keep-alive");
    headers.put("Cache-Control", "max-age=0");
    headers.put("Cookie", "did=web_fb03fe0b03e54f3488eaa1141f80da2e; didv=1667484473000; kpf=PC_WEB; kpn=KUAISHOU_VISION; clientid=3; client_key=65890b29");
    headers.put("Host", "v.kuaishou.com");
    headers.put("User-Agent", USER_AGENT);

    final String method = "GET";
    HttpURLConnection conn = (HttpURLConnection) new URL(referer).openConnection();
    conn.setRequestMethod(method);
    for (final String key : headers.keySet()) {
      conn.addRequestProperty(key, headers.get(key));
    }
    if ("POST".equals(method)) {
      conn.setDoOutput(true);
      conn.getOutputStream().write(data.getBytes());
    }
    final int code = conn.getResponseCode();
    Log.d(TAG, "tryFinal() " + conn.getHeaderField(null) + ", mime: " + conn.getContentType());

    dumpHeaders(conn, "final step");

    // stream to string
    String result;
    if (code >= 200 && code < 400) {
      result = DyUtil.streamAsString(conn.getInputStream(), conn.getContentEncoding());
    } else {
      result = DyUtil.streamAsString(conn.getErrorStream(), conn.getContentEncoding());
    }
    // Split by line, then ensure each line can fit into Log's maximum length.
    for (int i = 0, length = result.length(); i < length; i++) {
      int newline = result.indexOf('\n', i);
      newline = newline != -1 ? newline : length;
      do {
        int limit = Math.min(newline, i + MAX_LOG_LENGTH);
        Log.d(TAG, "tryFinal() " + result.substring(i, limit));
        i = limit;
      } while (i < newline);
    }
    return null;
  }

  private static final int MAX_LOG_LENGTH = 4000;
}
