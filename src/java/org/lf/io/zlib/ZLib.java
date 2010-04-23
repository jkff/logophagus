package org.lf.io.zlib;

import com.sun.jna.Library;
import com.sun.jna.Native;

interface ZLib extends Library {

    public static ZLib INSTANCE = (ZLib) Native.loadLibrary(System.getProperty("os.name").equals("Windows") ? "zlib1" : "libz", ZLib.class);

    int inflatePrime(z_stream stream, int bits, int value);

    int inflateSetDictionary(z_stream stream, byte[] dictionary, int length);

    int inflateInit2_(z_stream stream, int windowBits, String version, int streamSize);

    int inflate(z_stream stream, int flush);

    int inflateEnd(z_stream stream);

    String zlibVersion();

    public static final int Z_OK = 0;
    public static final int Z_STREAM_END = 1;
    public static final int Z_NEED_DICT = 2;
    public static final int Z_ERRNO = -1;
    public static final int Z_STREAM_ERROR = -2;
    public static final int Z_DATA_ERROR = -3;
    public static final int Z_MEM_ERROR = -4;
    public static final int Z_BUF_ERROR = -5;
    public static final int Z_VERSION_ERROR = -6;

    public static final int Z_BLOCK = 5;
    public static final int Z_NO_FLUSH = 0;

    public static final int STREAM_SIZE = new z_stream().size();

}
