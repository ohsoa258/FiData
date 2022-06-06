package com.fisk.task.dto.daconfig;

import lombok.Data;

/**
 * ftp配置项
 *
 * @author cfk
 */
@Data
public class FtpConfig {
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
    /*
     * 第一个sheet页名称
     * */
    public String sheetName;


}
