package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.GenerationType;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 *  快捷命令
 *
 */
@Data
@Entity
@RegisterForReflection
public class ShortcutCmd extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    /**
     * 快捷名称  用于检索
     */
    private String name;

}
