package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName tq_data_flow_scheduling
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_data_flow_scheduling")
@Data
public class TqDataFlowSchedulingPO extends BasePO implements Serializable {

    /**
     * 场景
     */
    @ApiModelProperty(value = "场景")
    private String scene;

    /**
     * 管道
     */
    @ApiModelProperty(value = "管道")
    private String pipe;

    /**
     * 调度时间
     */
    @ApiModelProperty(value = "调度时间")
    private String scheduleTime;

    /**
     * 调度结果 1成功 0失败
     */
    @ApiModelProperty(value = "调度结果 1成功 0失败")
    private Integer result;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}