package com.fisk.dataaccess.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import com.fisk.dataaccess.dto.ftp.ExcelTreeDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.sftp.SftpPreviewQueryDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.enums.FtpFileTypeEnum;
import com.fisk.dataaccess.service.ISftp;
import com.fisk.dataaccess.utils.ftp.ExcelUtils;
import com.fisk.dataaccess.utils.sftp.SftpUtils;
import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
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

    @Override
    public List<ExcelDTO> previewContent(SftpPreviewQueryDTO dto) {
        ChannelSftp sftp = null;
        try {
            AppDataSourcePO dataSourcePo = dataSourceImpl.query().eq("app_id", dto.appId).one();
            if (dataSourcePo == null) {
                throw new FkException(ResultEnum.FTP_CONNECTION_INVALID);
            }
            sftp = getChannelSftp(dataSourcePo);

            //根据路径名,截取后缀名等信息
            List<String> excelParam = SftpUtils.encapsulationExcelParam(dto.fileFullName);

            //获取InputStream流
            InputStream inputStream = SftpUtils.getSftpFileInputStream(sftp, dto.fileFullName);

            FtpFileTypeEnum fileTypeEnum = FtpFileTypeEnum.getValue(dataSourcePo.fileSuffix);

            switch (fileTypeEnum) {
                // 获取excel内容
                case XLS_FILE:
                case XLSX_FILE:
                    return ExcelUtils.readExcelFromInputStream(inputStream, excelParam.get(2));
                case CSV_FILE:
                    return ExcelUtils.readCsvFromInputStream(inputStream, excelParam.get(3));
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        } catch (Exception e) {
            log.error("SFTP预览数据失败,{}", e);
            throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        } finally {
            SftpUtils.disconnect(sftp);
        }

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
