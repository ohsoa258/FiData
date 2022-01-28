package com.fisk.dataservice.vo.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version v1.0
 * @description api信息，含订阅信息
 * @date 2022/1/19 19:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiSubVO extends ApiConfigVO {
    /**
     * 是否订阅 1：已订阅 0：未订阅
     */
    @ApiModelProperty(value = "是否订阅 1：已订阅 0：未订阅")
    public int apiSubState;
}
