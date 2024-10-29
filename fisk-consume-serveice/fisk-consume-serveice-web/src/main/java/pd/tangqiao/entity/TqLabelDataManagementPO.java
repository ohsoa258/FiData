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
 * @TableName tq_label_data_management
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="tq_label_data_management")
@Data
public class TqLabelDataManagementPO extends BasePO implements Serializable {

    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    private Integer ruleId;

    /**
     * 标签名
     */
    @ApiModelProperty(value = "标签名")
    private String lableName;

    /**
     * 状态 0禁用 1启用
     */
    @ApiModelProperty(value = "状态 0禁用 1启用")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}