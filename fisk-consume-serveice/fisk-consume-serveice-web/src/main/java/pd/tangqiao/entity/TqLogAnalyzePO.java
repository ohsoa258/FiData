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
 * @TableName tq_log_analyze
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_log_analyze")
@Data
public class TqLogAnalyzePO extends BasePO implements Serializable {

    /**
     * 服务名称
     */
    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    /**
     * 日志收集数量
     */
    @ApiModelProperty(value = "日志收集数量")
    private String logCount;

    /**
     * 错误日志类别占比
     */
    @ApiModelProperty(value = "错误日志类别占比")
    private String errorPercent;

    /**
     * 错误日志生成时间
     */
    @ApiModelProperty(value = "错误日志生成时间")
    private Date logTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}