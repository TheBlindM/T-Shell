package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * 历史命令
 *
 * @author TheBlind
 * @date 2022/8/26
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class HistoryCmd extends PanacheEntityBase {

    @Id
    @GeneratedValue
    private int id;

    private String cmdText;

    private String sessionId;

    private String createTime;

}
