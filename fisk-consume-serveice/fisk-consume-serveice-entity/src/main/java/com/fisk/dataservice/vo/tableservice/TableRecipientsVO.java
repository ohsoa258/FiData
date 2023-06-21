package com.fisk.dataservice.vo.tableservice;

import com.fisk.dataservice.dto.tableservice.WechatUserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表服务应用收件人
 * @date 2023/6/21 13:48
 */
@Data
public class TableRecipientsVO {
    /**
     * 表服务应用Id
     */
    @ApiModelProperty(value = "表服务应用Id")
    public int tableAppId;

    /**
     * 通知服务器Id
     */
    @ApiModelProperty(value = "通知服务器Id")
    public int noticeServerId;

    /**
     * 企业微信用户列表
     */
    @ApiModelProperty(value = "企业微信用户列表")
    public List<WechatUserVO> wechatUserList;

    /**
     * 用户邮箱，多个分号分隔
     */
    @ApiModelProperty(value = "用户邮箱，多个分号分隔")
    public String userEmails;
}
