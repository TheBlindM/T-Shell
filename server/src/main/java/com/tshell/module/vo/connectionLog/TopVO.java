package com.tshell.module.vo.connectionLog;

import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Objects;

/**
 * @author TheBlind
 * @version 1.0
 */
@Data
public class TopVO {
    private  String sessionName;
    private  String sessionId;
}
