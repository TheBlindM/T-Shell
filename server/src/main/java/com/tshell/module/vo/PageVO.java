package com.tshell.module.vo;

import java.util.List;

public record PageVO<T>(
        int count,
        List<T> list
) {
}
