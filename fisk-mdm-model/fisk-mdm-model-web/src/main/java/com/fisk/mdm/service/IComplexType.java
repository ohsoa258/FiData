package com.fisk.mdm.service;

import com.fisk.mdm.dto.complextype.ComplexTypeDetailsParameterDTO;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.vo.complextype.EchoFileVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author JianWenYang
 * @date 2022-06-01 18:18
 */
public interface IComplexType {

    /**
     * 添加经纬度
     *
     * @param dto
     * @return
     */
    Integer addGeography(GeographyDTO dto);

    /**
     * 添加经纬度
     * @param dto
     * @param connection
     * @throws SQLException
     */
    void addGeography(GeographyDTO dto, Connection connection) throws SQLException;

    /**
     * 上传文件
     *
     * @param versionId
     * @param file
     * @return
     */
    EchoFileVO uploadFile(Integer versionId, MultipartFile file);

    /**
     * 获取复杂类型数据详情
     *
     * @param dto
     * @param response
     * @return
     */
    Object getComplexTypeDetails(ComplexTypeDetailsParameterDTO dto, HttpServletResponse response);

}
