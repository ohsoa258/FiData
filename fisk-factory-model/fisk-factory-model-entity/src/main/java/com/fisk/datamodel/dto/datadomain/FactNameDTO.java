package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactNameDTO {
    public Long factId;
    public String factTabName;
    /**
     * 3.业务域 4.业务流程  5.事实表
     */
    public Integer flag;
    /**
     * 业务过程id
     */
    public Long pid;
}
