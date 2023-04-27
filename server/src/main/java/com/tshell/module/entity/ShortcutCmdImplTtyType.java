package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    int shortcutCmdImplId;

    private int ttyTypeId;

}
