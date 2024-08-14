package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * 
 * @author wangjian
 * @date 2024-01-30 10:09:37
 */
@TableName("tb_code_set")
@Data
public class CodeSetPO extends BasePO {

	@ApiModelProperty(value = "集合Id")
	public Integer collectionId;

	@ApiModelProperty(value = "编号")
	private String code;

	@ApiModelProperty(value = "名称")
	private String name;

	@ApiModelProperty(value = "描述")
	private String description;
}
