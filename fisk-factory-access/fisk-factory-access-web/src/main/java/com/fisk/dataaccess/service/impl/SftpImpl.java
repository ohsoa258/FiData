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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
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

    @Resource
    @Value("${sftp.file-path}")
    public String filePath;

    @Override
    public ResultEnum connectSftp(DbConnectionDTO dto) {
        ChannelSftp connect = SftpUtils.connect(dto.host, Integer.parseInt(dto.port), dto.connectAccount, dto.connectPwd, dto.connectStr);
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

    @Override
    public String uploadSecretKeyFile(Integer appId, MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String path = filePath + appId;
            //如果不存在,创建文件夹
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }

            //指定到上传路径
            String filePath = path + "/" + fileName;

            //创建新文件对象 指定文件路径为拼接好的路径
            File newFile = new File(filePath);
            //将前端传递过来的文件输送给新文件 这里需要抛出IO异常 throws IOException
            file.transferTo(newFile);

            return filePath;
        } catch (IOException e) {
            log.error("sftp上传秘钥文件失败,{}", e);
            throw new FkException(ResultEnum.UPLOAD_ERROR);
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
                dataSourcePo.connectPwd,
                dataSourcePo.connectStr);
    }

}
