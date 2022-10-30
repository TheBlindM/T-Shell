package com.tshell.core;


public record FileInfo(
        String path,
        String name,
        long size,
        FileType type,
        String modifyDate
) {

}
