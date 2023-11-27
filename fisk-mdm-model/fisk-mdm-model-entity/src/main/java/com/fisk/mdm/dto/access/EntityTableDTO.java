package com.fisk.mdm.dto.access;

import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-22
 * @Description:
 */
@Data
public class EntityTableDTO {
    private Integer id;

    @ApiModelProperty(value = "表名称")
    private String tableName;

    @ApiModelProperty(value = "表字段信息")
    private List<AttributeInfoDTO> fields;
}
