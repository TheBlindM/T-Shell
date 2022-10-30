package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 快捷命令组
 */
@Data
@Entity
public class GroupShortcutCmd extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;

    private int shortcutCmdGroupId;

    private int shortcutCmdId;

}
