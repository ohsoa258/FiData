package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则搜索条件
 * @date 2022/4/12 18:17
 */
@Data
public class DataCheckRuleSearchWhereMapVO {
    /**
     * 文本
     */
    @ApiModelProperty(value = "文本")
    public Object text;

    /**
     * 值
     */
    @ApiModelProperty(value = "值")
    public Object value;

    /**
     * 值
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
}
