package com.fisk.mdm.service;

import com.fisk.mdm.dto.complextype.GeographyDTO;

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

}
