package com.tshell.module.dto.fileManager;

import com.tshell.core.FileType;

public record CreateDTO(String path,
                        String name,
                        FileType type) {
}
