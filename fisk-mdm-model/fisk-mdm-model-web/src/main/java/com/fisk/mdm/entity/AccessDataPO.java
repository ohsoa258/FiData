package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author wangjian
 */
@TableName("tb_access_data")
@Data
public class AccessDataPO extends BasePO implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "模型id")
	private Integer modelId;

	@ApiModelProperty(value = "实体id")
	private Integer entityId;

	@ApiModelProperty(value = "源系统")
	private Integer souceSystemId;

	@ApiModelProperty(value = "抽取脚本")
	private String extractionSql;

	@ApiModelProperty(value = "数据接入类型 1：全量 2：增量 3：追加 ")
	private Integer accessType;

	@ApiModelProperty(value = "加载脚本")
	private String loadingSql;

}
