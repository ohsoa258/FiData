package com.fisk.datamodel.dto;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DwFieldQueryDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id ")
    public Integer tblId;

    @ApiModelProperty(value = "表类型 TableBusinessTypeEnum")
    public Integer tblType;

    /**
     * 发布状态 0：未发布 1：已发布，只有表节点才有发布状态;   该参数都需要传
     */
    @ApiModelProperty(value = "发布状态 0：未发布 1：已发布，只有表节点才有发布状态")
    public String publishState;

}
