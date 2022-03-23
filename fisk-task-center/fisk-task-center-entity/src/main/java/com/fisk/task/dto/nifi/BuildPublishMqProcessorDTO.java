package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildPublishMqProcessorDTO extends BaseProcessorDTO{
    public String exchange;
    public String route;
    public String host;
    public String port;
    public String vhost;
    public String user;
    public String pwd;
}
