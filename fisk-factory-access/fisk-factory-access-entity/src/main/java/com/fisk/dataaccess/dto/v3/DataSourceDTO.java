package com.fisk.dataaccess.dto.v3;

import com.fisk.dataaccess.dto.table.DataBaseViewDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DataSourceDTO {

    @ApiModelProperty(value = "id")
    public long id;

    /**
     * 数据源
     */
    @ApiModelProperty(value = "数据源")
    public String driveType;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;
    /**
     * 表
     */
    @ApiModelProperty(value = "表")
    public List<TablePyhNameDTO> tableDtoList;
    /**
     * 视图
     */
    @ApiModelProperty(value = "视图")
    public List<DataBaseViewDTO> viewDtoList;
}
