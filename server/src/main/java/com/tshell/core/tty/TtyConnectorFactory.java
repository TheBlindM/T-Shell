package com.tshell.core.tty;

import com.tshell.core.JschPtyConnector;
import com.tshell.core.Parameter;
import com.jcraft.jsch.JSchException;

import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * @author TheBlind
 * @date 2022/7/22
 */
@Singleton
public  class TtyConnectorFactory {



    public TtyConnector getTtyConnector(Parameter parameter) throws IOException {
        /**
         *  jdk 12  支持 case 多个  箭头操作符,无需break switch 不仅可以作为语句，也可以作为表达式。
         *  jdk 14 yield 处理复杂逻辑时返回值  switch 返回数据
         *  jdk 17 支持 null 以及 Object 为switch中的参数
         */
        switch (parameter) {
            case Parameter.SshParameter s -> {
                return new JschPtyConnector(s);
//                return new SshTtyConnector(s);
            }
           /* case Parameter p -> {
                return new LocalTtyConnector(p);
            }*/
            default -> throw new IllegalStateException("Unexpected value: " + parameter);
        }
    }

}
