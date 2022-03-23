package com.fisk.dataaccess.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.dto.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;

import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源接口
 * @date 2021/12/28 10:50
 */
public interface IFtp {
    /**
     * 测试ftp数据源连接
     *
     * @param dto dto
     * @return 连接结果
     */
    ResultEntity<Object> connectFtp(DbConnectionDTO dto);

    /**
     * 点击文件预览内容
     *
     * @param query query
     * @return 文本内容
     */
    List<ExcelDTO> previewContent(OdsQueryDTO query);

    /**
     * 加载ftp文件系统
     *
     * @param dto
     * @return dto
     */
    ExcelTreeDTO loadFtpFileSystem(FtpPathDTO dto);
}
