package com.fisk.datafactory.dto.customworkflow;

import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class DispatchEmailDTO extends BasePO {

    /**
     * 邮件服务器id
     */
    public int emailserverConfigId;
    /**
     * 管道id,数字id
     */
    public int nifiCustomWorkflowId;
    /**
     * 通知类别,1邮件,2短信,3微信
     */
    public int type;
    /**
     * 收件人
     */
    public String recipients;
    /**
     * 报错信息
     */
    public String msg;

}
