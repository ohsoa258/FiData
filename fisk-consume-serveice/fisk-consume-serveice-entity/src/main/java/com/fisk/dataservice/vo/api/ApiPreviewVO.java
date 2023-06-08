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
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public Integer current;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小")
    public Integer size;

    /**
     * 总条数
     */
    @ApiModelProperty(value = "total")
    public Integer total;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "page")
    public Integer page;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<FieldConfigVO> fieldVO;
}
