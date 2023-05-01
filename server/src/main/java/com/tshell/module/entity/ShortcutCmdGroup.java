package com.tshell.module.entity;


import cn.hutool.core.util.StrUtil;
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
public class ShortcutCmdGroup extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    /**
     * 快捷名称  用于检索
     */
    private String groupName;
    private int parentId;

    public void copyProperty(ShortcutCmdGroup source) {
        copyProperty(source, false);
    }


    public void copyProperty(ShortcutCmdGroup source, boolean ignoreNull) {
        String sourceGroupName = source.getGroupName();


        if (ignoreNull && StrUtil.isNotBlank(sourceGroupName)) {
            this.setGroupName(sourceGroupName);
        } else if (!ignoreNull) {
            this.setGroupName(sourceGroupName);
        }

      /*  Integer sourceParentId = source.getParentId();
        if (ignoreNull && sourceParentId != null) {
            this.setParentId(sourceParentId);
        } else {
            this.setParentId(sourceParentId);
        }*/

    }
}
