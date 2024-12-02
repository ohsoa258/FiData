package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据集校验查询DTO
 * @date 2022/3/24 13:21
 */
@Data
public class DataSetQueryDTO {
    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int size;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    public int current;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public long templateId;
}
