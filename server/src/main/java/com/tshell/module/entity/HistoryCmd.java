package com.tshell.module.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String cmdText;

    private String sessionId;

    private String createTime;

}
