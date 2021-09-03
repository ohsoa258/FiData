package com.fisk.dataaccess.vo;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiVO {
    /**
     * 用户id
     */
    public Long userId;
    /**
     * 应用注册id
     */
    public String appId;
    /**
     * 物理表id
     */
    public String tableId;
    /**
     * nifi流程回写的组件
     */
    public String componentId;
}
