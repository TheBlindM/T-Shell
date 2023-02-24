package com.tshell.module.entity;

import com.tshell.config.AuthTypeConverter;
import com.tshell.module.enums.AuthType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

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

    protected String privateKeyFile;
    protected String passphrase;
    @Convert(converter = AuthTypeConverter.class)
    private AuthType authType;

}
