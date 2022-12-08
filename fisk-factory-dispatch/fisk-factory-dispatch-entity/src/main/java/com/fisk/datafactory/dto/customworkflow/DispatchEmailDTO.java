package com.fisk.datafactory.dto.customworkflow;

import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.datafactory.enums.SendModeEnum;
import lombok.Data;

import java.util.Map;

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

    /**
     * false只失败发  ,true成功也发,默认失败才发
     */
    public Integer sendMode;
    /**
     * 管道名称
     */
    public String pipelName;
    /**
     * 运行结果
     */
    public String result;
    /**
     * 运行时长
     */
    public String duration;
    /**
     * TraceID
     */
    public String pipelTraceId;
    /**
     * url
     */
    public String url;
    /**
     * body
     */
    public Map<String, String> body;
}
