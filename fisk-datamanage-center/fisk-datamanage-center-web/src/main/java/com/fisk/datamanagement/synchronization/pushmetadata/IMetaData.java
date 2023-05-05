package com.fisk.datamanagement.synchronization.pushmetadata;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetaData {

    /**
     * 新增元数据
     *
     * @param data
     * @return
     */
    ResultEnum metaData(MetaDataAttributeDTO data);

    /**
     * 消费元数据
     *
     * @param data
     * @return
     */
    ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data,String currUserName);


    ResultEnum addFiledAndUpdateFiled(List<MetaDataInstanceAttributeDTO> data);

    /**
     * 删除元数据实体
     *
     * @param dto
     * @return
     */
    ResultEnum deleteMetaData(MetaDataDeleteAttributeDTO dto);

    /**
     * 同步表级业务元数据
     *
     * @param dto
     */
    void synchronousTableBusinessMetaData(BusinessMetaDataInfoDTO dto);


}
