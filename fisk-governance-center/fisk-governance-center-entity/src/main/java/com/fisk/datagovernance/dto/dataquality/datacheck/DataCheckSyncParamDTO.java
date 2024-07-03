package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验参数DTO
 * @date 2023/7/7 15:24
 */
@Data
public class DataCheckSyncParamDTO {
    /**
     * 修改条件字段-SQL
     */
    @ApiModelProperty(value = "条件字段-SQL")
    public String whereFieldSql;

    /**
     * 校验通过修改字段-SQL
     */
    @ApiModelProperty(value = "校验通过修改字段-SQL")
    public String successFieldSql;

    /**
     * 校验不通过修改字段-SQL
     */
    @ApiModelProperty(value = "校验不通过修改字段-SQL")
    public String failFieldSql;

    /**
     * 校验警告修改字段-SQL
     */
    @ApiModelProperty(value = "校验警告修改字段-SQL")
    public String warnFieldSql;

    /**
     * 消息字段-字段名称
     */
    @ApiModelProperty(value = "消息字段-字段名称")
    public String msgField;

    /**
     * 唯一标识字段，依据此字段回写表数据
     */
    @ApiModelProperty(value = "唯一标识字段，依据此字段回写表数据")
    public String uniqueField;

    /**
     * 唯一标识字段，依据此字段回写表数据(未被格式化)
     */
    @ApiModelProperty(value = "唯一标识字段，依据此字段回写表数据")
    public String uniqueIdNameUnFormat;

    /**
     * 表名称-带架构名
     */
    @ApiModelProperty(value = "表名称-带架构名")
    public String tableName;

    /**
     * 表名称-带转义处理带架构名
     */
    @ApiModelProperty(value = "表名称-带转义处理带架构名")
    public String tableNameFormat;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段名称-带转义处理
     */
    @ApiModelProperty(value = "字段名称-带转义处理")
    public String fieldNameFormat;

    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号")
    public String batchNumber;

    /**
     * 小批次号
     */
    @ApiModelProperty(value = "小批次号")
    public String smallBatchNumber;

    /**
     * 请求参数DTO
     */
    @ApiModelProperty(value = "请求参数DTO")
    public DataCheckSyncDTO requestDto;

}
