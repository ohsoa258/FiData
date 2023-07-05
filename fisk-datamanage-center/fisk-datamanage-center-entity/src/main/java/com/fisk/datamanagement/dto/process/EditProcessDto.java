package com.fisk.datamanagement.dto.process;

import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class EditProcessDto {

    /**
     * 流程ID
     */
    public String guid;
    /**
     * 输入参数集合
     */
    @ApiModelProperty(value = "输入参数集合")
    public List<String> inputList;
    /**
     * process名称
     */
    @ApiModelProperty(value = "process名称")
    public String processName;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String description;
    /**
     * 输出参数guid
     */
    @ApiModelProperty(value = "输出参数guid")
    public String outGuid;
}
