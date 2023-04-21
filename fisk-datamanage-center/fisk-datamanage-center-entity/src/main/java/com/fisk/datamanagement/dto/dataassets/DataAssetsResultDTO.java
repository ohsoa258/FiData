package com.fisk.datamanagement.dto.dataassets;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataAssetsResultDTO {
    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "数据列表")
    public JSONArray dataArray;

    @ApiModelProperty(value = "专栏列表")
    public List<String[]> columnList;

    @ApiModelProperty(value = "页面索引")
    public int pageIndex;

    @ApiModelProperty(value = "页面大小")
    public int pageSize;

    @ApiModelProperty(value = "合计")
    public int total;
}
