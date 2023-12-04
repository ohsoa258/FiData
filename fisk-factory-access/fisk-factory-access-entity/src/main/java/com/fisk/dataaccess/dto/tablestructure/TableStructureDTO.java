package com.fisk.dataaccess.dto.tablestructure;

import com.fisk.dataaccess.dto.sapbw.CubeDimsAndMeas;
import com.fisk.dataaccess.dto.sapbw.CubeVariable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 表结构对象
 * </p>
 *
 * @author Lock
 */
@Data
public class TableStructureDTO {
    /**
     * 字段名
     */
    @ApiModelProperty(value = "字段名", required = true)
    public String fieldName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度", required = true)
    public int fieldLength;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;

    /**
     * 字段精度
     */
    @ApiModelProperty(value = "字段精度", required = true)
    public Integer fieldPrecision;

    /**
     * 如果是sapbw，这个对象代表cube的所有参数
     */
    @ApiModelProperty(value = "如果是sapbw，这个对象代表cube的所有参数")
    public CubeVariable cubeVariable;

    /**
     * 如果是sapbw，这个对象代表cube的所有维度和指标
     */
    @ApiModelProperty(value = "如果是sapbw，这个对象代表cube的所有维度和指标")
    public CubeDimsAndMeas cubeDimsAndMeas;

    /**
     * 源库名称
     */
    @ApiModelProperty(value = "源库名称", required = true)
    public String sourceDbName;

    /**
     * 源表名称
     */
    @ApiModelProperty(value = "源表名称", required = true)
    public String sourceTblName;

    /**
     * 是否主键  0否   1是
     */
    @ApiModelProperty(value = "是否主键")
    public Integer isPk;

}
