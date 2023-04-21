package com.fisk.datamanagement.dto.dataassets;

import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataAssetsParameterDTO {

    /**
     * 实例guid
     */
    @ApiModelProperty(value = "guid")
    public String instanceGuid;
    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String dbName;
    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String columnName;
    /**
     * 当前页
     */
    @ApiModelProperty(value = "当前页")
    public int pageIndex;
    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int pageSize;
    /**
     * 筛选条件
     */
    @ApiModelProperty(value = "筛选条件")
    public List<FilterQueryDTO> filterQueryDTOList;
    /**
     * 是否导出
     */
    @ApiModelProperty(value = "是否导出")
    public boolean export;

}
