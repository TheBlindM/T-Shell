package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GenerationType;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * 快捷命令组
 */
@Data
@Entity
public class GroupShortcutCmd extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    private int shortcutCmdGroupId;

    private int shortcutCmdId;

}
