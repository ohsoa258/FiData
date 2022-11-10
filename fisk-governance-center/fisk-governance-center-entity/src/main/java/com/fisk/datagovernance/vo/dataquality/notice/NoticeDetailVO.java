package com.fisk.datagovernance.vo.dataquality.notice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 添加警告通知VO
 * @date 2022/3/22 15:38
 */
@Data
public class NoticeDetailVO {
    /**
     * 数据校验规则通知列表
     */
    @ApiModelProperty(value = "数据校验规则通知列表")
    public List<NoticeModuleVO> noticeRule_DataCheck;

    /**
     * 业务清洗规则通知列表
     */
    @ApiModelProperty(value = "业务清洗规则通知列表")
    public List<NoticeModuleVO> noticeRule_BusinessFilter;

    /**
     * 生命周期规则通知列表
     */
    @ApiModelProperty(value = "生命周期规则通知列表")
    public List<NoticeModuleVO> noticeRule_Lifecycle;

    /**
     * 邮件服务器列表
     */
    @ApiModelProperty(value = "邮件服务器列表")
    public List<NoticeEmailVO> emailServerVOS;

    /**
     * 数据校验规则关联的通知ID
     */
    @ApiModelProperty(value = "数据校验规则关联的通知ID")
    public List<Long> noticeIds_DataCheck;

    /**
     * 业务清洗规则关联的通知ID
     */
    @ApiModelProperty(value = "业务清洗规则关联的通知ID")
    public List<Long> noticeIds_BusinessFilter;

    /**
     * 生命周期规则关联的通知ID
     */
    @ApiModelProperty(value = "生命周期规则关联的通知ID")
    public List<Long> noticeIds_Lifecycle;
}
