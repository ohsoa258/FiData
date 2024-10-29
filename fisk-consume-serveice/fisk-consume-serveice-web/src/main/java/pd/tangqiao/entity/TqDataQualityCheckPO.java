package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @TableName tq_data_quality_check
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="tq_data_quality_check")
@Data
public class TqDataQualityCheckPO extends BasePO implements Serializable {

    /**
     * 数据项
     */
    @ApiModelProperty(value = "数据项")
    private String dataOpt;

    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号")
    private String batchCode;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    /**
     * 状态 0不符合规范 1符合规范
     */
    @ApiModelProperty(value = "状态 0不符合规范 1符合规范")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}