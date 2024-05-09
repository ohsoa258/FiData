package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2024-04-23 15:35:47
 */
@TableName("tb_datacheck_standards_group")
@Data
public class DatacheckStandardsGroupPO extends BasePO {

    @ApiModelProperty(value = "校验组名称")
    private String checkGroupName;

    @ApiModelProperty(value = "数据标准MenuId")
    private Integer standardsMenuId;

    @ApiModelProperty(value = "数据标准id")
    private Integer standardsId;

    @ApiModelProperty(value = "属性中文名称")
    private String chineseName;

    @ApiModelProperty(value = "属性英文名称")
    private String englishName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "数据元编号")
    private String datametaCode;
}
