package com.tshell.module.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;



@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(name = "nanoId", strategy = "com.tshell.config.NanoIdGenerator")
public  class Breakpoint extends PanacheEntityBase {
    public Breakpoint(String transferRecordId, long start, long current, long end) {
        this.transferRecordId = transferRecordId;
        this.start = start;
        this.current = current;
        this.end = end;
    }

    @Id
    @GeneratedValue(generator = "nanoId")
    private String id;
    private String transferRecordId;
    private  long start;
    private  long current;
    private  long end;


}
