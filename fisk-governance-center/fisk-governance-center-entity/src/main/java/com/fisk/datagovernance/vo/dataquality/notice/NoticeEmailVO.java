package com.fisk.datagovernance.vo.dataquality.notice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/11/7 13:53
 */
@Data
public class NoticeEmailVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;
}
