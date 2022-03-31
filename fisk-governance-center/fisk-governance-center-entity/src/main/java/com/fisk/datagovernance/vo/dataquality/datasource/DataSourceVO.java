package com.fisk.datagovernance.vo.dataquality.datasource;

import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源
 * @date 2022/3/22 14:51
 */
public class DataSourceVO
{
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int id;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
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
