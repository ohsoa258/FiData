package com.fisk.dataaccess.dto.datamanagement;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 14:50
 */
@Data
public class DataAccessSourceTableDTO {

    @ApiModelProperty(value = "物理表id")
    public long id;

    @ApiModelProperty(value = "物理表名称")
    public String tableName;

    public List<DataAccessSourceFieldDTO> list;
}
