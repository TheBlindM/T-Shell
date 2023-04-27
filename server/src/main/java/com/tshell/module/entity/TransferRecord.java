package com.tshell.module.entity;


import com.tshell.core.FileOperate;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * @author TheBlind
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(name = "nanoId", strategy = "com.tshell.config.NanoIdGenerator")
public class TransferRecord extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "nanoId")
    public String id;
    private String sessionId;
    private String createTime;
    private String readPath;
    private String writePath;
    private FileOperate operate;
    private Status status;

    public enum Status {

        /**
         * 完成
         */
        COMPLETE,
        /**
         * 暂停
         */
        PAUSE,
        /**
         * 进行
         */
        PROCESS,
        /**
         * 等待
         */
        WAIT

    }


}
