package com.fisk.datamodel.dto.businessprocess;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessPublishQueryDTO {

    @ApiModelProperty(value = "业务域Id")
    public int businessAreaId;

    @ApiModelProperty(value = "事实Id")
    public List<Integer> factIds;
    /**
     * 发布备注
     */
    @ApiModelProperty(value = "备注")
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
