package com.fisk.task.dto.nifi;

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
    public String httpMethod;
    /*
     *Remote URL
     */
    public String remoteUrl;
    /*
     *Content-Type
     */
    public String contentType;
    /*
     *Attributes to Send
     */
    public String attributesToSend;

}
