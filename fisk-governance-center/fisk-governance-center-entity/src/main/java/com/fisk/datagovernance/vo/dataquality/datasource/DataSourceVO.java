package com.fisk.datagovernance.vo.dataquality.datasource;

import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源
 * @date 2022/3/22 14:51
 */
@Data
public class DataSourceVO
{
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int id;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    public DataSourceTypeEnum conType;

    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    public String name;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    public String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * FiData数据源id
     */
    @ApiModelProperty(value = "FiData数据源id")
    public int datasourceId;

    /**
     * 数据源类型 1、FiData 2、自定义
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum datasourceType;

    /**
     * 表
     */
    @ApiModelProperty(value = "表")
    public List<TablePyhNameDTO> tableDtoList;

    /**
     * 视图
     */
    //@ApiModelProperty(value = "视图")
    //public List<DataBaseViewDTO> viewDtoList;
}
