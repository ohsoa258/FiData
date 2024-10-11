package com.fisk.datagovernance.entity.monitor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
@TableName("tb_monitor_recipients")
public class MonitorRecipientsPO extends BasePO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "tb_emailserver_config表主键id")
    private Integer noticeServerId;

    @ApiModelProperty(value = "企业微信用户Id")
    private String wechatUserId;

    @ApiModelProperty(value = "企业微信用户名称")
    private String wechatUserName;

    @ApiModelProperty(value = "用户邮箱，多个分号分隔")
    private String userEmails;

    @ApiModelProperty(value = "类型:1、邮箱 2、企业微信")
    private Integer type;

    @ApiModelProperty(value = "启用状态 1、启用 2、禁用")
    private Integer enable;

    @ApiModelProperty(value = "预警级别(EmailWarnLevelEnum)")
    private Integer warnLevel;
}
