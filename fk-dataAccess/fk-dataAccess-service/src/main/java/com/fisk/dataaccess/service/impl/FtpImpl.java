package com.fisk.dataaccess.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.DbConnectionDTO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.ftpUtils.FTPUtils;
import com.fisk.dataaccess.service.IFtp;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源实现类
 * @date 2021/12/28 10:50
 */
@Service
public class FtpImpl implements IFtp {


    @Override
    public ResultEntity<Object> connectFtp(DbConnectionDTO dto) {
        FTPClient ftpClient = null;
        try {
            if (DataSourceTypeEnum.FTP.getName().equals(dto.driveType)) {
                ftpClient = FTPUtils.connectFtpServer(dto.host, Integer.parseInt(dto.port), dto.connectAccount, dto.connectPwd, "utf-8");
                return ftpClient.isConnected() ? ResultEntityBuild.build(ResultEnum.SUCCESS) : ResultEntityBuild.build(ResultEnum.FTP_CONNECTION_ERROR);
            }
        } finally {
            // 关闭连接
            FTPUtils.closeFTPConnect(ftpClient);
        }
        return null;
    }
}
