package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表服务通知收件人
 * @date 2023/6/21 13:36
 */
@Data
@TableName("tb_table_recipients")
public class TableRecipientsPO extends BasePO {
    /**
     * 表服务应用Id
     */
    public int tableAppId;

    /**
     * 通知服务器Id
     */
    public int noticeServerId;

    /**
     * 发送类型 1.邮箱 2.企业微信
     */
    public int type;


    /**
     * 启用状态 1、启用 2、禁用
     */
    public int enable;

    /**
     * 企业微信用户Id
     */
    public String wechatUserId;

    /**
     * 企业微信用户名称
     */
    public String wechatUserName;

    /**
     * 用户邮箱，多个分号分隔
     */
    public String userEmails;

    /**
     * 告警条件：1 成功、2 失败、3 完成
     */
    public int alarmConditions;

    /**
     * 预警级别(EmailWarnLevelEnum)
     */
    public Integer warnLevel;
}
