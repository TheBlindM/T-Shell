package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * @author TheBlind
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(name = "nanoId", strategy = "com.tshell.config.NanoIdGenerator")
public class Session extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "nanoId")
    public String id;

    private String sessionName;
    private int ttyTypeId;
    /**
     * 0 为ssh
     * 1 为local
     */
    private int type;

    private int sessionGroupId;

    private String updateTime;

}

