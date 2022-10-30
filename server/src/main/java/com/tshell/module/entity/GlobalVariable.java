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
 * ${xx}
 * 全局变量
 * @author TheBlind
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class GlobalVariable extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;
    private String varName;
    private String value;

}
