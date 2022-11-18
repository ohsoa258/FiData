package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildFetchSFTPProcessorDTO extends BaseProcessorDTO {
    public String hostname;
    public String port;
    public String username;
    public String password;
    public String remoteFile;
}
