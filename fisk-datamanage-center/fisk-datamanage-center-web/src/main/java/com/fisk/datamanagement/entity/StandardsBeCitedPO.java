package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2023-11-20 13:56:24
 */
@TableName("tb_standards_be_cited")
@Data
public class StandardsBeCitedPO extends BasePO {

    @ApiModelProperty(value = "数据源ID")
    public Integer dbId;

    @ApiModelProperty(value = "数据标准表id")
    private Integer standardsId;

    @ApiModelProperty(value = "数据库名称")
    private String databaseName;

    @ApiModelProperty(value = "数据表Id")
    private String tableId;

    @ApiModelProperty(value = "数据表名称")
    private String tableName;

    @ApiModelProperty(value = "表业务类型")
    private TableBusinessTypeEnum tableBusinessType;

    @ApiModelProperty(value = "架构名称")
    private String schemaName;

    @ApiModelProperty(value = "表字段Id")
    private String fieldId;

    @ApiModelProperty(value = "表字段名称")
    private String fieldName;

    @ApiModelProperty(value = "数据源名称")
    private String datasourceName;

}
