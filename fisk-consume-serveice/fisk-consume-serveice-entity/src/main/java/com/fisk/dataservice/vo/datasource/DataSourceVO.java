package com.fisk.dataservice.vo.datasource;

import com.fisk.common.datadriven.sqlDto.TablePyhNameDTO;
import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据源
 * @date 2022/1/14 18:27
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
