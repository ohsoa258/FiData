package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 预览结果集
 * @date 2022/1/16 12:52
 */
@Data
public class DataSetPreviewVO
{
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<String> fieldList;
}
