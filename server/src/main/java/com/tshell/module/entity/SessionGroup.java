package com.tshell.module.entity;

import cn.hutool.core.util.StrUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionGroup extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public int id;
    private String groupName;
    private int parentId;

}
