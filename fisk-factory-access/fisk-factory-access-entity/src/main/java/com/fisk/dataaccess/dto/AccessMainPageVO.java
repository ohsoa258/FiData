package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AccessMainPageVO {

    /**
     * 接口总数
     */
    @ApiModelProperty(value = "接口总数")
    private Integer interfaceCount;

    /**
     * 数据
     */
    @ApiModelProperty(value = "数据总数")
    private Long dataCount;

    /**
     * 重点接口总数
     */
    @ApiModelProperty(value = "重点接口总数")
    private Integer ImportantInterfaceCount;

    /**
     * 数据存储大小
     */
    @ApiModelProperty(value = "数据存储大小")
    private Integer DatastoreSize;

    /**
     * 成功次数
     */
    @ApiModelProperty(value = "成功次数")
    private Integer successCount;

    /**
     * 失败次数
     */
    @ApiModelProperty(value = "失败次数")
    private Integer failCount;

}
