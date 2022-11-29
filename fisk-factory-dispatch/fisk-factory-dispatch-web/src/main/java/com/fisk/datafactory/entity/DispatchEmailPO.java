package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;


/**
 * @author cfk
 */
@Data
@TableName("tb_dispatch_email")
public class DispatchEmailPO extends BasePO {
    /**
     * 邮件服务器id
     */
    public int emailserverConfigId;
    /**
     * 管道id
     */
    public int nifiCustomWorkflowId;
    /**
     * 通知类别
     */
    public int type;
    /**
     * 收件人
     */
    public String recipients;
    /**
     * false只失败发  ,true成功也发,默认失败才发
     */
    public boolean sendMode;




}
