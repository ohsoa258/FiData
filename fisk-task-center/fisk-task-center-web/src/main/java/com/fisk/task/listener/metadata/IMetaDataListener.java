package com.fisk.task.listener.metadata;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author JianWenYang
 */
public interface IMetaDataListener {

    /**
     * 元数据实时同步
     *
     * @param dataInfo
     * @param acknowledgment
     * @return
     */
    ResultEnum metaData(String dataInfo, Acknowledgment acknowledgment);

}
