package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author TheBlind
 * @date 2022/7/14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@RegisterForReflection
public class ShortcutCmdImpl extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;
    /**
     * 命令模板   支持占位符 如    ${全局变量}
     */
    String cmdTemplate;
    private int shortcutCmdId;


}
