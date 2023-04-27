package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GenerationType;
import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@Data
public class GroupHost extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public int id;
  private int sessionGroupId;
  private  int hostId;

}
