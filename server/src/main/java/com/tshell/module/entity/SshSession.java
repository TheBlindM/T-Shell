package com.tshell.module.entity;

import cn.hutool.core.util.StrUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author TheBlind
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(name = "nanoId", strategy = "com.tshell.config.NanoIdGenerator")
public class SshSession extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "nanoId")
    public String id;
    private String sessionId;
    private String ip;
    private Integer port;
    private String username;
    private String pwd;

}
