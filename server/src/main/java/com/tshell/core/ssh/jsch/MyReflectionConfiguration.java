package com.tshell.core.ssh.jsch;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;

/**
 * @author TheBlind
 * @version 1.0
 * @date 2022/10/13 9:37
 */
@RegisterForReflection(targets={ DefaultEvictionPolicy.class})
public class MyReflectionConfiguration {
}
