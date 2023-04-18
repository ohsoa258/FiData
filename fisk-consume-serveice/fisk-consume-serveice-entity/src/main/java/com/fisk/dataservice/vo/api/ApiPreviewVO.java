package com.fisk.dataservice.vo.api;

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
public class ApiPreviewVO
{
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;

    /**
     * 数据总条数
     */
    @ApiModelProperty(value = "数据总条数")
    public int totalCount;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<FieldConfigVO> fieldVO;
}
