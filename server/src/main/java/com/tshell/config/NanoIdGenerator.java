package com.tshell.config;

import cn.hutool.core.util.IdUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * @author TheBlind
 */
@RegisterForReflection
public class NanoIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {

        return IdUtil.nanoId();
    }
}
