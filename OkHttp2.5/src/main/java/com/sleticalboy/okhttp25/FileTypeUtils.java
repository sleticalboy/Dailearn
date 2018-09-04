package com.sleticalboy.okhttp25;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created on 18-9-3.
 *
 * @author sleticalboy
 */
public final class FileTypeUtils {

    private static final Map<String, String> mFileTypes = new ArrayMap<>();
    private static final String UNKNOWN_KEY = "000000";
    private static final String UNKNOWN_VALUE = "unknown";

    static {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //other
        mFileTypes.put("41433130", "dwg"); // CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); // 日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "docx");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
        mFileTypes.put(UNKNOWN_KEY, UNKNOWN_VALUE);
    }

    public static String getFileType(@NonNull String filePath) {
        return getFileType(new File(filePath));
    }

    public static String getFileType(@NonNull File file) {
        if (!file.exists()) {
            return null;
        }
        try {
            return getType(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private static String getType(@NonNull InputStream inputStream) {
        String header;
        try {
            byte[] b = new byte[4];
            // int read() 从此输入流中读取一个数据字节。
            // int read(byte[] b) 从此输入流中将最多 b.length 个字节的数据读入一个 byte 数组中。
            // int read(byte[] b, int off, int len) 从此输入流中将最多 len 个字节的数据读入一个 byte 数组中。
            inputStream.read(b, 0, b.length);
            header = bytesToHexString(b);
        } catch (Exception ignored) {
            header = UNKNOWN_KEY;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        return mFileTypes.get(header);
    }

    /**
     * 将要读取文件头信息的文件的byte数组转换成string类型表示
     *
     * @param src 要读取文件头信息的文件的byte数组
     * @return 文件头信息
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (final byte b : src) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }
}
