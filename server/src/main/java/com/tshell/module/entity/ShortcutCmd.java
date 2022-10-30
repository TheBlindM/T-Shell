package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *  快捷命令
 *
 */
@Data
@Entity
@RegisterForReflection
public class ShortcutCmd extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;

    /**
     * 快捷名称  用于检索
     */
    private String name;

}
