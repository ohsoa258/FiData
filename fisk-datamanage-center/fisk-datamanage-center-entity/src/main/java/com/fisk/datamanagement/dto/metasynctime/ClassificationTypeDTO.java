package com.fisk.datamanagement.dto.metasynctime;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ClassificationTypeDTO {

    /**
     * 服务id
     */
    @ApiModelProperty(value = "服务id")
    private Integer id;

    /**
     * 服务英文名
     */
    @ApiModelProperty(value = "服务英文名")
    private ClassificationTypeEnum typeENName;

    /**
     *服务中文名
     */
    @ApiModelProperty(value = "服务中文名")
    private String typeCNName;

}
