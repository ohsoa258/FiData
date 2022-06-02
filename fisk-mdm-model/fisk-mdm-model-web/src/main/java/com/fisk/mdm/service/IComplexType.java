package com.fisk.mdm.service;

import com.fisk.mdm.dto.complextype.GeographyDTO;
import org.springframework.web.multipart.MultipartFile;

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
     * 上传文件
     *
     * @param file
     * @return
     */
    Integer uploadFile(MultipartFile file);

}
