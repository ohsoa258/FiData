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
 * @TableName tq_question_bank
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="tq_question_bank")
@Data
public class TqQuestionBankPO extends BasePO implements Serializable {

    /**
     * 场景
     */
    @ApiModelProperty(value = "场景")
    private String scene;

    /**
     * 数据项
     */
    @ApiModelProperty(value = "数据项")
    private String dataOpt;

    /**
     * 成功数据
     */
    @ApiModelProperty(value = "成功数据")
    private String successData;

    /**
     * 失败数据
     */
    @ApiModelProperty(value = "失败数据")
    private String failureData;

    /**
     * 规则配置
     */
    @ApiModelProperty(value = "规则配置")
    private String ruleConfig;

    /**
     * 状态 0待核查 1已核查
     */
    @ApiModelProperty(value = "状态 0待核查 1已核查")
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}