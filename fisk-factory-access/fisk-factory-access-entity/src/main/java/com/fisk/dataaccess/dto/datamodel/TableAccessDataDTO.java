package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableAccessDataDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "类型")
    public int type;

    /**
     * 表类型 OlapTableEnum
     */
    @ApiModelProperty(value = "表类型 OlapTableEnum")
    public Integer tableType;

    /**
     * 物理表显示名称
     */
    @ApiModelProperty(value = "物理表显示名称")
    public String displayName;

    @ApiModelProperty(value = "字段DTO列表")
    public List<TableFieldDataDTO> fieldDtoList;
}
