package com.fisk.datafactory.dto.customworkflow;

import com.fisk.datafactory.dto.UserInfoDTO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class RecipientsDTO {

    @ApiModelProperty(value = "发送邮箱id")
    public int dispatchEmailId;
    /**
     * 邮件服务器id
     */
    @ApiModelProperty(value = "邮件服务器id")
    public int emailserverConfigId;
    /**
     * 管道id,数字id
     */
    @ApiModelProperty(value = "管道id,数字id")
    public int nifiCustomWorkflowId;

    @ApiModelProperty(value = "通知类别 1、邮箱，2、企业微信")
    public int type;

    /**
     * 用户信息
     */
    @ApiModelProperty(value = "用户信息")
    public List<UserInfoDTO> userInfo;

    /**
     * false只失败发  ,true成功也发,默认失败才发
     */
    @ApiModelProperty(value = "false只失败发  ,true成功也发,默认失败才发")
    public Integer sendMode;

}

