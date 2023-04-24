package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author wangjian
 * @date 2023-04-18
 */
@TableName("tb_access_transformation")
@Data
public class AccessTransformationPO extends BasePO{

	@ApiModelProperty(value = "accessId")
	private Integer accessId;

	@ApiModelProperty(value = "来源表名称")
	private String sourceTableName;

	@ApiModelProperty(value = "来源字段名称")
	private String sourceFieldName;

	@ApiModelProperty(value = "属性id")
	private Integer attributeId;

	@ApiModelProperty(value = "是否业务主键 1：主键 0：不是主键 ")
	private Integer businessKey;

}
