package com.fisk.task.vo.tableservice;

import com.fisk.dataservice.vo.tableservice.WechatUserVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-24
 * @Description:
 */
@Data
public class TableServiceRecipientsVO {
    public String cronExpression;

    @ApiModelProperty(value = "通知服务器Id")
    public int noticeServerId;

    @ApiModelProperty(value = "企业微信用户列表")
    public List<WechatUserVO> wechatUserList;

    @ApiModelProperty(value = "用户邮箱，多个分号分隔")
    public String userEmails;

    @ApiModelProperty(value = "启用状态")
    public int enable;
}
