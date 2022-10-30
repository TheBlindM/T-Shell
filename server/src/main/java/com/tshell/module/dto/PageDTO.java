package com.tshell.module.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageDTO<T>(
        int page,
        int size,
        T param
) {
}
