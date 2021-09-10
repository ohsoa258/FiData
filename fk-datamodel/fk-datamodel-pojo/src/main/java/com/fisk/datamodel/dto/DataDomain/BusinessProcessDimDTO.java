package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/10 14:40
 * 业务过程 二级
 */
@Data
public class BusinessProcessDimDTO {
    public Long businessProcessId;
    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    public List<FactDimDTO> factList;

    /**
     * 7.业务域  8.维度  9.业务过程  10.事实表
     */
    public Integer flag;
    /**
     * 上一级id
     */
    public Long pid;
}
