package com.fisk.datamanagement.synchronization.pushmetadata;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.dto.metadata.MetaDataInstanceAttributeDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetaData {

    /**
     * 添加元数据
     *
     * @param data
     * @return
     */
    ResultEnum metaData(List<MetaDataInstanceAttributeDTO> data);

}
