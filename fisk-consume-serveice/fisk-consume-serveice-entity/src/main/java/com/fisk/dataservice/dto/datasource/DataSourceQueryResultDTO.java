package com.fisk.dataservice.dto.datasource;

import com.alibaba.fastjson.JSONArray;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceQueryResultDTO {

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "数据列表")
    public JSONArray dataArray;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "字段名称DTO列表")
    public List<FieldNameDTO> fieldNameDTOList;

    @ApiModelProperty(value = "页码索引")
    public int pageIndex;

    @ApiModelProperty(value = "页码大小")
    public int pageSize;

    @ApiModelProperty(value = "总计")
    public int total;

    @ApiModelProperty(value = "sql")
    public String sql;

}
