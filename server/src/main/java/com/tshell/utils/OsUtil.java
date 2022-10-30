package com.tshell.utils;

/**
 * @author TheBlind
 * @date 2022/7/20
 */
public class OsUtil {

    public static final String OS_NAME = System.getProperty("os.name");
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();

    protected static final String _OS_NAME = OS_NAME.toLowerCase();

    public static final boolean isWindows = _OS_NAME.startsWith("windows");
    public static final boolean isMac = _OS_NAME.startsWith("mac");
    public static final boolean isLinux = _OS_NAME.startsWith("linux");



}
