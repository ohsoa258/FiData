package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;

/**
 * @author JianWenYang
 */
public interface ISftp {

    /**
     * 测试连接sftp
     *
     * @param dto
     * @return
     */
    ResultEnum connectSftp(DbConnectionDTO dto);

    /**
     * 获取sftp文件和文件夹
     *
     * @param dto
     * @return
     */
    ExcelTreeDTO getFile(FtpPathDTO dto);

}
