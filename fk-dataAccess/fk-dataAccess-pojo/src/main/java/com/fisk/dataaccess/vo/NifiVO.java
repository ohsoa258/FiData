package com.fisk.dataaccess.vo;

import lombok.Data;

import java.util.List;

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
     * nifi流程回写的组件
     */
    public String componentId;
    /**
     * 物理表id
     */
    public List<Long> tableIdList;
}
