package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@Data
    public class ProcessRelationshipAttributesPutDTO {

    @ApiModelProperty(value = "类型名")    public String guid;
    public String typeName;
    @ApiModelProperty(value = "实体状态")
    public String entityStatus;

    @ApiModelProperty(value = "展示文本")
    public String displayText;
    @ApiModelProperty(value = "关联类型")
    public String relationshipType;
    @ApiModelProperty(value = "关联guid")
    public String relationshipGuid;
    @ApiModelProperty(value = "关联状态")
    public String relationshipStatus;
    @ApiModelProperty(value = "关联属性")
    public ProcessRelationShipAttributesTypeNameDTO relationshipAttributes;
}
