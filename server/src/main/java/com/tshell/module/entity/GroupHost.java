package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class GroupHost extends PanacheEntityBase {

  @Id
  @GeneratedValue
  public int id;
  private int sessionGroupId;
  private  int hostId;

}
