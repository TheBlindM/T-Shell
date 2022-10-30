package com.tshell.utils.io;

import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Assert;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author TheBlind
 * @date 2022/4/13
 */
public class IoUtil {

    private static final int DEFAULT_BUFFER_SIZE = 2 << 11;


    /**
     * 从流中读取内容，读取完成后关闭流
     *
     * @param in      输入流
     * @param charset 字符集
     * @return 内容
     * @throws IORuntimeException IO异常
     * @deprecated 请使用 {@link #read(InputStream, Charset)}
     */
    @Deprecated
    public static String read(InputStream in, Charset charset) throws IOException {
        final FastByteArrayOutputStream out = read(in);
        return out.toString(charset);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后关闭流
     *
     * @param in 输入流
     * @return 输出流
     * @throws IORuntimeException IO异常
     */
    public static FastByteArrayOutputStream read(InputStream in) throws IOException {
        return read(in, true);
    }

    public static String readUtf8(InputStream in) throws IOException {
        return read(in, StandardCharsets.UTF_8);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后可选是否关闭流
     *
     * @param in      输入流
     * @param isClose 读取完毕后是否关闭流
     * @return 输出流
     * @throws IORuntimeException IO异常
     * @since 5.5.3
     */
    public static FastByteArrayOutputStream read(InputStream in, boolean isClose) throws IOException {
        final FastByteArrayOutputStream out;
        if (in instanceof FileInputStream) {
            // 文件流的长度是可预见的，此时直接读取效率更高
            try {
                out = new FastByteArrayOutputStream(in.available());
            } catch (IOException e) {
                throw new IORuntimeException(e);
            }
        } else {
            out = new FastByteArrayOutputStream();
        }
        try {
            in.transferTo(out);
        } finally {
            if (isClose) {
                close(in);
            }
        }
        return out;
    }


    public static Integer copy(InputStream in, OutputStream out) throws IOException{
     return copy(Channels.newChannel(in),Channels.newChannel(out),DEFAULT_BUFFER_SIZE);
    }


    /**
     * 拷贝流，使用NIO，不会关闭流
     *
     * @param in         {@link ReadableByteChannel}
     * @param out        {@link WritableByteChannel}
     * @param bufferSize 缓冲大小，如果小于等于0，使用默认
     * @return 拷贝的字节数
     */
    public static Integer copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize) throws IOException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize <= 0 ? DEFAULT_BUFFER_SIZE : bufferSize);
        Integer size = 0;

        while (in.read(byteBuffer) != -1) {
            //翻转
            byteBuffer.flip();
            // 写转读
            size += out.write(byteBuffer);
            byteBuffer.clear();
        }
        return size;
    }


    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }


}
