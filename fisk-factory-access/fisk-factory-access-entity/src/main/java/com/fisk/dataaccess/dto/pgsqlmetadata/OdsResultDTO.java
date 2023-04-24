package com.fisk.dataaccess.dto.pgsqlmetadata;

import com.alibaba.fastjson.JSONArray;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OdsResultDTO {
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;
    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<FieldNameDTO> fieldNameDTOList;

    @ApiModelProperty(value = "页索引")
    public int pageIndex;

    @ApiModelProperty(value = "页大小")
    public int pageSize;

    @ApiModelProperty(value = "总数")
    public int total = 0;

    @ApiModelProperty(value = "sql")
    public String sql;
}
