package com.fisk.task.dto.daconfig;

import lombok.Data;

/**
 * 数据接入配置项
 * @author gy
 */
@Data
public class DataAccessConfigDTO {
    /**
     * 组配置
     */
    public GroupConfig groupConfig;

    /**
     * 数据源jdbc连接
     */
    public DataSourceConfig sourceDsConfig;

    /**
     * 目标源jdbc连接
     */
    public DataSourceConfig targetDsConfig;

    public ProcessorConfig processorConfig;
}
