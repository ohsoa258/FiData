package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author wangjian
 */
@TableName("tb_access_data")
@Data
public class AccessDataPO extends BasePO {

	@ApiModelProperty(value = "模型id")
	private Integer modelId;

	@ApiModelProperty(value = "实体id")
	private Integer entityId;

	@ApiModelProperty(value = "源系统")
	private Integer souceSystemId;
	@ApiModelProperty(value = "发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布")
	public int isPublish;

	@ApiModelProperty(value = "抽取脚本")
	private String extractionSql;

	@ApiModelProperty(value = "1：追加、2：全量、3：业务主键覆盖、4：业务时间覆盖； ")
	private Integer accessType;

	@ApiModelProperty(value = "加载脚本")
	private String loadingSql;

}
