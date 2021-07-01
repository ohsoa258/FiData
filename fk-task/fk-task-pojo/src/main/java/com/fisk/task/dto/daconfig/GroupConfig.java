package com.fisk.task.dto.daconfig;

import lombok.Data;

/**
 * @author gy
 */
@Data
public class GroupConfig {
    public String appName;

    public String appDetails;

    /**
     * 是否需要创建新的项目
     */
    public boolean newApp;

    /**
     * 组件id
     */
    public String componentId;
}
