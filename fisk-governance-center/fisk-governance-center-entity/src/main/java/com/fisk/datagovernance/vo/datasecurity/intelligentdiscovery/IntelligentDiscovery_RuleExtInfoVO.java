package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_RuleExtInfoVO {
    /**
     * FiData平台用户列表
     */
    @ApiModelProperty(value = "FiData平台用户列表")
    public List<IntelligentDiscovery_RuleExtInfo_UserVO> fiDataUsers;

    /**
     * 数据源列表
     */
    @ApiModelProperty(value = "数据源列表")
    public List<IntelligentDiscovery_RuleExtInfo_DataSourceVO> dataSources;
}
