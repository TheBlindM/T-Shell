package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * 选项
 *
 *  git [--version] [--help] [-C <path>]
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;

     private int cmdId;

     private String jsonNames;
    /**
     * 描述
     */
    private String description;

}
