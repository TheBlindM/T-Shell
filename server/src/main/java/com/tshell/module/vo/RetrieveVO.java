package com.tshell.module.vo;

import com.tshell.service.RetrieveService;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author TheBlind
 * @date 2022/7/17
 */
@RegisterForReflection
public record RetrieveVO( 
        String label, String value, RetrieveService.RetrieveItemType type
) {
}
