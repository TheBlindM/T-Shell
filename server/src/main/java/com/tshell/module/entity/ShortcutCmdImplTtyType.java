package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 *
 * @author TheBlind
 * @date 2022/7/14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ShortcutCmdImplTtyType extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;

    int shortcutCmdImplId;

    private int ttyTypeId;

}
