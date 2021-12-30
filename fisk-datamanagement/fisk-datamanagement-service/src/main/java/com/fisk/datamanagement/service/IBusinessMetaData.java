package com.fisk.datamanagement.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataDTO;

/**
 * @author JianWenYang
 */
public interface IBusinessMetaData {

    /**
     * 获取业务元数据列表
     * @return
     */
    BusinessMetaDataDTO getBusinessMetaDataList();

    /**
     * 添加元数据以及属性
     * @param dto
     * @return
     */
    ResultEnum addBusinessMetaData(BusinessMetaDataDTO dto);

    /**
     * 修改业务元数据属性
     * @param dto
     * @return
     */
    ResultEnum updateBusinessMetaData(BusinessMetaDataDTO dto);

    /**
     * 根据业务元数据名称删除
     * @param businessMetaDataName
     * @return
     */
    ResultEnum deleteBusinessMetaData(String businessMetaDataName);

}
