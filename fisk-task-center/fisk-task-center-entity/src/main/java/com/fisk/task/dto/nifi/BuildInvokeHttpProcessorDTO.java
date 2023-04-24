package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildInvokeHttpProcessorDTO extends BaseProcessorDTO {
    /*
     *HTTP Method
     */
    @ApiModelProperty(value = "请求方法")
    public String httpMethod;
    /*
     *Remote URL
     */
    @ApiModelProperty(value = "文件路径")
    public String remoteUrl;
    /*
     *Content-Type
     */
    @ApiModelProperty(value = "消息类型")
    public String contentType;
    /*
     *Attributes to Send
     */
    @ApiModelProperty(value = "发送属性")
    public String attributesToSend;

    /*
     * nifitoken
     * */
    @ApiModelProperty(value = "nifi令牌")
    public String nifiToken;

}
