package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/10 14:42
 * 事实表 三级
 */
@Data
public class FactDimDTO {
    public Long factId;
    public String factTableEnName;
    /**
     * 7.业务域  8.维度  9.业务过程  10.事实表
     */
    public Integer flag;
    /**
     * 上一级id
     */
    public Long pid;
}
