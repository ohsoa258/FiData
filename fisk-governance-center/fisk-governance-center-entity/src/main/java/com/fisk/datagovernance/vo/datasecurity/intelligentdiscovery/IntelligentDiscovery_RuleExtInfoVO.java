package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_RuleExtInfoVO {

    /**
     * 登录用户信息
     */
    @ApiModelProperty(value = "登录用户信息")
    public IntelligentDiscovery_RuleExtInfo_UserInfoVO userInfo;

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

    /**
     * 邮件服务器列表
     */
    @ApiModelProperty(value = "邮件服务器列表")
    public List<IntelligentDiscovery_RuleExtInfo_EmailServiceVO> emailServices;
}
