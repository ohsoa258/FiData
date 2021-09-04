package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 17:17
 * 业务过程 二级
 */
@Data
public class BusinessProcessNameDTO {

    public Long businessProcessId;
    public String businessProcessCnName;
    /**
     * 事实表 三级
     */
    public List<FactNameDTO> factList;
    /**
     * 3.业务域 4.业务流程  5.事实表
     */
    public Integer flag;
    /**
     * 业务域的id
     */
    public Long pid;
}
