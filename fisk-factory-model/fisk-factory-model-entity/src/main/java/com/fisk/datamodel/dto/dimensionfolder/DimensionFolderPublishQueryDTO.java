package com.fisk.datamodel.dto.dimensionfolder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderPublishQueryDTO {
    @ApiModelProperty(value = "业务域Id")
    public int businessAreaId;
    @ApiModelProperty(value = "维度Id")
    public List<Integer> dimensionIds;
    /**
     * 发布备注
     */
    @ApiModelProperty(value = "发布备注")
    public String remark;
    /**
     * 增量配置
     */
    @ApiModelProperty(value = "增量配置")
    public int syncMode;
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "是否同步")
    public boolean openTransmission;
}
