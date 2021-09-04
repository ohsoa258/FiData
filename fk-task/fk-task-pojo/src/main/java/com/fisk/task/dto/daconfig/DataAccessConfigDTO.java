package com.fisk.task.dto.daconfig;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import lombok.Data;

import java.util.List;

/**
 * 数据接入配置项
 *
 * @author gy
 */
@Data
public class DataAccessConfigDTO {
    /**
     * app组配置
     */
    public GroupConfig groupConfig;

    /**
     * 任务组配置
     */
    public TaskGroupConfig taskGroupConfig;

    /**
     * 数据源jdbc连接
     */
    public DataSourceConfig sourceDsConfig;

    /**
     * 目标源jdbc连接
     */
    public DataSourceConfig targetDsConfig;

    /**
     * 增量配置库源jdbc连接
     */
    public DataSourceConfig cfgDsConfig;

    public ProcessorConfig processorConfig;
    /**
     * 物理表字段
     */
    public List<TableFieldsDTO> tableFieldsList;
}
