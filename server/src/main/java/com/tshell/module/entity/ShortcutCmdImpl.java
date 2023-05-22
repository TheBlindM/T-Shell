package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @GeneratedValue(strategy=GenerationType.TABLE,generator="table_gen")
    @TableGenerator(
            name = "table_gen",
            initialValue = 50, //初始化值
            allocationSize=3 //累加值
    )
    public Integer id;
    /**
     * 命令模板   支持占位符 如    ${全局变量}
     */
    String cmdTemplate;
    private int shortcutCmdId;


}
