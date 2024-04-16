package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-04-11 16:45:35
 */
@TableName("tb_business_category_assignment")
@Data
public class BusinessCategoryAssignmentPO extends BasePO {

    @ApiModelProperty(value = "指标目录id")
    private Integer categoryId;

    @ApiModelProperty(value = "角色id")
    private Integer roleId;

}
