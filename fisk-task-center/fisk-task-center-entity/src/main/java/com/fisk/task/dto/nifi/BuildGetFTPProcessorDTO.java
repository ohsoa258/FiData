package com.fisk.task.dto.nifi;

import lombok.Data;
/**
 * @author cfk
 */
@Data
public class BuildGetFTPProcessorDTO extends BaseProcessorDTO {
    /*
     * ip
     * */
    public String hostname;
    /*
     * 端口号
     * */
    public String port;

    public String username;
    /*
     * 密码
     * */
    public String password;
    /*
     * 文件路径
     * */
    public String remotePath;
    /*
     * 文件名称,加扩展名(全名称)
     * */
    public String fileFilterRegex;
    /*
     * 是否启用utf-8
     * */
    public boolean ftpUseUtf8;
}
