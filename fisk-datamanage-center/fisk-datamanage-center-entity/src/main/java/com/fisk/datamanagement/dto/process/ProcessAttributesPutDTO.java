package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@Data
public class ProcessAttributesPutDTO {
    @ApiModelProperty(value = "guid")
    public String guid;
    @ApiModelProperty(value = "类型名称")
    public String typeName;
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "独特属性")
    public ProcessUniqueAttributesDTO uniqueAttributes;
}
