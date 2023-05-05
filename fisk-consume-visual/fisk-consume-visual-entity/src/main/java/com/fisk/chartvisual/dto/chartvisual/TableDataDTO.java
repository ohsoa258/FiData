package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.DataDoFieldTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableDataDTO {
    /**
     * 字段所属表id
     */
    @ApiModelProperty(value = "id")
    public Integer id;
    /**
     * 字段类型: 筛选器  列  值
     */
    @ApiModelProperty(value = "类型")
    public DataDoFieldTypeEnum type;
    /**
     * 表所属字段
     */
    @ApiModelProperty(value = "表所属字段")
    public String tableField;
    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    public String tableName;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
    /**
     * 别名
     */
    @ApiModelProperty(value = "别名")
    public String alias;
    /**
     * 关联哪个维度表id
     */
    @ApiModelProperty(value = "关联哪个维度表id")
    public Integer relationId;
    /**
     * 关联维度的表名
     */
    @ApiModelProperty(value = "关联维度的表名")
    public String dimensionName;
    /**
     * 表名的key
     */
    @ApiModelProperty(value = "表名的key")
    public String tableNameKey;
}
