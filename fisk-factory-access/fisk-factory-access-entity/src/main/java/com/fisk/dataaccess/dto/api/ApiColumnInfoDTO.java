package com.fisk.dataaccess.dto.api;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ApiColumnInfoDTO {

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "字段名称DTO列表")
    public List<FieldNameDTO> fieldNameDTOList;

}
