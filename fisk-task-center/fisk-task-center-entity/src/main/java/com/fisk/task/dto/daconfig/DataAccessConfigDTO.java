package com.fisk.task.dto.daconfig;

import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "app组配置")
    public GroupConfig groupConfig;

    /**
     * 任务组配置
     */
    @ApiModelProperty(value = "任务组配置")
    public TaskGroupConfig taskGroupConfig;

    /**
     * 数据源jdbc连接
     */
    @ApiModelProperty(value = "数据源jdbc连接")
    public DataSourceConfig sourceDsConfig;

    /**
     * 目标源jdbc连接
     */
    @ApiModelProperty(value = "目标源jdbc连接")
    public DataSourceConfig targetDsConfig;

    /**
     * 增量配置库源jdbc连接
     */
    @ApiModelProperty(value = "增量配置库源jdbc连接")
    public DataSourceConfig cfgDsConfig;

    @ApiModelProperty(value = "处理器配置")
    public ProcessorConfig processorConfig;

    /**
     * 增量对象
     */
    @ApiModelProperty(value = "增量对象")
    public TableBusinessDTO businessDTO;

    /**
     * 业务主键集合(逗号隔开)
     */
    @ApiModelProperty(value = "业务主键集合(逗号隔开)")
    public String businessKeyAppend;

    /*
     * 建模字段详情list
     * */
    @ApiModelProperty(value = "建模字段详情list")
    public List<ModelPublishFieldDTO> modelPublishFieldDTOList;

    /*
     * ftp配置信息
     * */
    @ApiModelProperty(value = "ftp配置信息")
    public FtpConfig ftpConfig;


}
