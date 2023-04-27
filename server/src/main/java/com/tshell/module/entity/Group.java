package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GenerationType;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Data
@Entity
public class Group extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public int id;
  private String groupName;


}
