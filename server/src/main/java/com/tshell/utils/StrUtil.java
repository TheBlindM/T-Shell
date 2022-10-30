package com.tshell.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author TheBlind
 * @date 2022/4/12
 */
public class StrUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static byte[] bytes(String str, Charset charset) {
        if (str == null) {
            return null;
        }

        if (null == charset) {
            return str.getBytes();
        }
        return str.getBytes(charset);
    }

    public static String getUtf8String(String str) {
        if (str != null && str.length() > 0) {
            try {
                byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
                String encode;
                boolean pdUtf = false;
                pdUtf = validUtf8(bytes);
                if (pdUtf) {
                    encode = String.valueOf(StandardCharsets.UTF_8);
                } else {
                    encode = "GBK";
                }
                str = new String(str.getBytes(StandardCharsets.ISO_8859_1), encode);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static boolean validUtf8(byte[] data) {
        int i = 0;
        int count = 0;
        while (i < data.length) {
            int v = data[i];
            if (count == 0) {
                if ((v & 240) == 240 && (v & 248) == 240) {
                    count = 3;
                } else if (((v & 224) == 224) && (v & 240) == 224) {
                    count = 2;
                } else if ((v & 192) == 192 && (v & 224) == 192) {
                    count = 1;
                } else if ((v | 127) == 127) {
                    count = 0;
                } else {
                    return false;
                }
            } else {
                if ((v & 128) == 128 && (v & 192) == 128) {
                    count--;
                } else {
                    return false;
                }
            }

            i++;
        }

        return count == 0;
    }



}
