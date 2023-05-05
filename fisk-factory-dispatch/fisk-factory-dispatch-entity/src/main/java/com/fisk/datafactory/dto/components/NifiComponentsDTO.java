package com.fisk.datafactory.dto.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiComponentsDTO {
    @ApiModelProperty(value = "1:开始;2:任务组;3:数据湖表任务;4:数仓维度表任务;" +
            "5:数仓事实表任务;6:分析模型维度表任务;7:分析模型事实表任务;" +
            "8:分析模型宽表任务;9:数据湖ftp任务;10:数据湖非实时api任务", required = true)
    public long id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "类型")
    public String type;

    @ApiModelProperty(value = "标记")
    public boolean flag;
}
