package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.dataquality.DataSourceConfigDTO;

/**
 * @author JianWenYang
 */
public interface IDataQuality {

    /**
     * 根据配置文件索引，获取数据源配置信息
     * @param index
     * @return
     */
    DataSourceConfigDTO getDataSourceConfig(int index);

}
