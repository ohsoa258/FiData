package com.fisk.dataaccess.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.sftp.SftpExcelTreeDTO;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import com.fisk.dataaccess.dto.ftp.FtpPathDTO;
import com.fisk.dataaccess.dto.sftp.SftpPreviewQueryDTO;
import com.fisk.dataaccess.dto.sftp.SftpUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    SftpExcelTreeDTO getFile(FtpPathDTO dto);

    /**
     * 预览文件
     *
     * @param dto
     * @return
     */
    List<ExcelDTO> previewContent(SftpPreviewQueryDTO dto);

    /**
     * 上传秘钥文件
     *
     * @param abbreviationName
     * @param file
     * @return
     */
    SftpUploadDTO uploadSecretKeyFile(String abbreviationName, MultipartFile file);

}
