package com.fisk.datamodel.dto.widetableconfig;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableQueryPageDTO {
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;

    @ApiModelProperty(value = "字段集合")
    public List<String> columnList;

    @ApiModelProperty(value = "生成的查询语句")
    public String sqlScript;

    @ApiModelProperty(value = "生成的更新语句")
    public String updateSqlScript;

    @ApiModelProperty(value = "配置DTO")
    public WideTableFieldConfigDTO configDTO;

}
