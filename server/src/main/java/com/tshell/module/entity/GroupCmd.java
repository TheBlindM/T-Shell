package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class GroupCmd extends PanacheEntityBase {


    @Id
    @GeneratedValue
    public int id;
    private Integer groupId;
    private Integer fastCmdId;

}
