package com.tshell.module.entity;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tshell.core.client.TtyType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作系统类型
 * 大部分情况下  子系统都会兼容父系统的命令
 * <p>
 * 如出现不兼容的情况 在Cmd中设置兼容的系统和不兼容的系统
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OsType extends PanacheEntityBase {


    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String osTypeName;

    private int parentId;

    private String startCommand;

    private boolean parent;


    public void copyProperties(OsType source) {
        copyProperty(source, false);
    }

    public void copyProperty(OsType source, boolean ignoreNull) {

        String sourceOsTypeName = source.getOsTypeName();
        if ((ignoreNull && StrUtil.isNotBlank(sourceOsTypeName)) || !ignoreNull) {
            this.setOsTypeName(sourceOsTypeName);
        }

  /*      Integer sourceParentId = source.getParentId();
        if ((ignoreNull && sourceParentId != null) || !ignoreNull) {
            this.setParentId(sourceParentId);
        }*/
    }


    /**
     * 默认 的类型
     */
    public enum DefaultPtyOsType {
        CMD(1), POWER_SHELL(2), LINUX_BASH(3), UBUNTU_BASH(4);

        private int id;

        DefaultPtyOsType(int id) {
            this.id = id;
        }

        public static TtyType getById(Integer id) {
            Assert.notNull(id);
            int intValue = id.intValue();
            for (TtyType ttyType : TtyType.values()) {
                if (intValue == ttyType.getId()) {
                    return ttyType;
                }
            }
            throw new IllegalArgumentException("没有该 PtyOsType");
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }


}
