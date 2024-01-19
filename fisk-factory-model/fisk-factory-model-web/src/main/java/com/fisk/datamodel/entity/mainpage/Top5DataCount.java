package com.fisk.datamodel.entity.mainpage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Top5DataCount {

    /**
     * dim Top5维度表数据量
     */
    @ApiModelProperty(value = "dim Top5维度表数据量")
    private List<Map.Entry<String, Long>> Top5DimDataCount;

    /**
     * fact Top5事实表数据量
     */
    @ApiModelProperty(value = "fact Top5事实表数据量")
    private List<Map.Entry<String, Long>> Top5FactDataCount;

    /**
     * help Top5帮助表数据量
     */
    @ApiModelProperty(value = "help Top5帮助表数据量")
    private List<Map.Entry<String, Long>> Top5HelpDataCount;

    /**
     * config Top5配置表数据量
     */
    @ApiModelProperty(value = "config Top5配置表数据量")
    private List<Map.Entry<String, Long>> Top5ConfigDataCount;

    /**
     * DWD Top5数据量
     */
    @ApiModelProperty(value = "DWD Top5数据量")
    private List<Map.Entry<String, Long>> Top5DwdDataCount;

    /**
     * DWS Top5数据量
     */
    @ApiModelProperty(value = "DWS Top5数据量")
    private List<Map.Entry<String, Long>> Top5DwsDataCount;

}
