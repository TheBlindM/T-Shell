package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
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
 * *   使用又划
 * *
 * *           ip配置
 * *
 * *  ipconfig {/all,/a}[显示完整信息]  /are 展示ipv6       ifconfig
 * *
 * *  实现  实现命令的ostype 不得设置 为父命令中的ostype
 * *
 * *             完整ip配置
 * *
 * *   ipconfig all
 * *
 * *
 * *   Git配置
 * *   主  git --version
 * *
 * *   子   remote	//查看远程库的信息
 * *       remote -v	//查看远程库的详细信息
 * *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cmd extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    private int parentCmdId;

    private String cmdText;

    private String describe;


}
