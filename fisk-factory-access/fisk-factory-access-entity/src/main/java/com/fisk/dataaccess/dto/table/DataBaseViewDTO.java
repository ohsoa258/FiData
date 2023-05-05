package com.fisk.dataaccess.dto.table;

import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description 数据库视图对象
 * @date 2021/12/31 14:38
 */
@Data
public class DataBaseViewDTO {

    @ApiModelProperty(value = "视图名称", required = true)
    public String viewName;

    @ApiModelProperty(value = "1: 当前视图有效; 2: 当前视图无效")
    public int flag;
    /**
     * 视图字段
     */
    @ApiModelProperty(value = "视图字段")
    public List<TableStructureDTO> fields;
}