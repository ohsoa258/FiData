package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tq_data_quality_task_management
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_data_quality_task_management")
@Data
public class TqDataQualityTaskManagementPO extends BasePO implements Serializable {

    /**
     * 数据项
     */
    @ApiModelProperty(value = "数据项")
    private String dataOption;

    /**
     * 数据批次
     */
    @ApiModelProperty(value = "数据批次")
    private String dataBatch;

    /**
     * 规则
     */
    @ApiModelProperty(value = "规则")
    private String rule;

    /**
     * 0不符合规范 1符合规范
     */
    @ApiModelProperty(value = "0不符合规范 1符合规范")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}