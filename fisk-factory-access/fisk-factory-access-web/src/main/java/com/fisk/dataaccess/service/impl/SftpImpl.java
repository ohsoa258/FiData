package com.fisk.dataaccess.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.FtpFileTypeEnum;
import com.fisk.dataaccess.service.ISftp;
import com.fisk.dataaccess.utils.sftp.SftpUtils;
import com.jcraft.jsch.ChannelSftp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class SftpImpl implements ISftp {

    @Resource
    AppDataSourceImpl dataSourceImpl;

    @Override
    public ResultEnum connectSftp(DbConnectionDTO dto) {
        ChannelSftp connect = SftpUtils.connect(dto.host, Integer.parseInt(dto.port), dto.connectAccount, dto.connectPwd);
        SftpUtils.disconnect(connect);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ExcelTreeDTO getFile(FtpPathDTO dto) {

        AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", dto.appId).one();
        if (dataSourcePo == null) {
            throw new FkException(ResultEnum.FTP_CONNECTION_INVALID);
        }
        ChannelSftp sftp = getChannelSftp(dataSourcePo);

        SftpUtils utils = new SftpUtils();
        FtpFileTypeEnum fileTypeEnum = FtpFileTypeEnum.getValue(dataSourcePo.fileSuffix);
        return utils.getFile(sftp, dto.fullPath, fileTypeEnum.getName());
    }

    /**
     * 根据应用，连接sftp
     *
     * @return
     */
    public ChannelSftp getChannelSftp(AppDataSourcePO dataSourcePo) {
        return SftpUtils.connect(
                dataSourcePo.host,
                Integer.parseInt(dataSourcePo.port),
                dataSourcePo.connectAccount,
                dataSourcePo.connectPwd);
    }

}
