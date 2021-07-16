package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import com.fisk.common.enums.task.MessageLevelEnum;
import com.fisk.common.enums.task.MessageStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_msg_log")
public class MessageLogPO extends BasePO {
    public String msg;
    public MessageStatusEnum status;
    public MessageLevelEnum level;
}
