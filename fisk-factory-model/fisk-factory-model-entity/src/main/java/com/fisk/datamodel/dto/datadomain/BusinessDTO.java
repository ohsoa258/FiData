package com.fisk.datamodel.dto.datadomain;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/6 10:21
 * 业务域
 */
@Data
public class BusinessDTO {
    public Long businessId;
    public String businessName;
    /**
     * 3.业务域 4.业务流程  5.事实表  6.只查询业务域的Tree
     */
    public Integer flag;
}
