package com.fisk.datamanagement.synchronization.pushmetadata;

import com.fisk.common.core.response.ResultEnum;
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
    ResultEnum metaData(List<MetaDataInstanceAttributeDTO> data);

    /**
     * 消费元数据
     *
     * @param data
     * @return
     */
    ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data);

}
