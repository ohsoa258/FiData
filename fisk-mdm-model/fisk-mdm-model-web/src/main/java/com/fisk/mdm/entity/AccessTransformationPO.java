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
public class AccessTransformationPO extends BasePO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "模型id")
	private Integer modelId;

	@ApiModelProperty(value = "实体id")
	private Integer entityId;

	@ApiModelProperty(value = "属性id")
	private Integer attributeId;

	@ApiModelProperty(value = "数据加载字段名")
	private String filedName;

	@ApiModelProperty(value = "是否业务主键 1：主键 0：不是主键 ")
	private Integer businessKey;

}
