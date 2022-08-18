package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表字段信息
 * @date 2022/8/18 12:08
 */
@Data
public class DataTableFielDTO {
    /**
     * 表/字段 Id
     */
    @ApiModelProperty(value = "表/字段 Id")
    public String id;

    /**
     * 表/字段 名称
     */
    @ApiModelProperty(value = "表/字段 名称")
    public String label;

    /**
     * 表/字段 别名名称
     */
    @ApiModelProperty(value = "表/字段 别名名称")
    public String labelAlias;

    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public TableBusinessTypeEnum tableBusinessTypeEnum;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<DataTableFielDTO> fields;
}
