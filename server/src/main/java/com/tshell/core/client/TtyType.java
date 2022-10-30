package com.tshell.core.client;

import cn.hutool.core.lang.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TheBlind
 * @date 2022/8/14
 */

public enum TtyType {
    CMD(1, "CMD", 0), POWER_SHELL(2, "POWER_SHELL", 0),
    LINUX_BASH(3, "LINUX_BASH", 0),
    UBUNTU_BASH(4, "UBUNTU_BASH", 3),
    LINUX_ZSH(5, "LINUX_ZSH", 3);

    private int id;
    private String name;
    private int parentId;

    TtyType(int id, String name, int parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
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


    public static List<Integer> getSubIdList(Integer id) {
        Assert.notNull(id);
        int intValue = id.intValue();
        List result = new ArrayList(2);
        for (TtyType ttyType : TtyType.values()) {
            if (intValue == ttyType.getParentId()) {
                result.add(ttyType.id);
            }
        }
        return result;
    }

    public static int getParentId(Integer id) {
        return getById(id).parentId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
