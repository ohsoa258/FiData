package com.fisk.dataservice.dto.tableservice;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
@Data
public class TableServiceEmailDTO extends BasePO {

    /**
     * 邮件服务器id
     */
    @ApiModelProperty(value = "邮件服务器id")
    public int emailserverConfigId;
    /**
     * 1:table 2:api
     */
    @ApiModelProperty(value = "1:table 2:api")
    public int appType;
    /**
     * 管道id,数字id
     */
    @ApiModelProperty(value = "appId")
    public int appId;
    /**
     * 通知类别,1邮件,2短信,3微信
     */
    @ApiModelProperty(value = "通知类别,1邮件,2微信,3信息")
    public int type;
    /**
     * 收件人
     */
    @ApiModelProperty(value = "收件人")
    public String recipients;

    @ApiModelProperty(value = "用户信息")
    public List<WechatUserDTO> userInfo;
    /**
     * 报错信息
     */
    @ApiModelProperty(value = "报错信息")
    public String msg;

    /**
     * false只失败发  ,true成功也发,默认失败才发
     */
    @ApiModelProperty(value = "false只失败发  ,true成功也发,默认失败才发")
    public Integer sendMode;
    /**
     * 管道名称
     */
    @ApiModelProperty(value = "管道名称")
    public String pipelName;
    /**
     * 运行结果
     */
    @ApiModelProperty(value = "运行结果")
    public String result;
    /**
     * 运行时长
     */
    @ApiModelProperty(value = "运行时长")
    public String duration;
    /**
     * TraceID
     */
    @ApiModelProperty(value = "TraceID")
    public String pipelTraceId;
    /**
     * url
     */
    @ApiModelProperty(value = "url")
    public String url;
    /**
     * body
     */
    @ApiModelProperty(value = "body")
    public Map<String, String> body;
}
