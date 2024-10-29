package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tq_log_collection
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_log_collection")
@Data
public class TqLogCollectionPO extends BasePO implements Serializable {

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 接口名称
     */
    @ApiModelProperty(value = "接口名称")
    private String apiName;

    /**
     * 数据记录数
     */
    @ApiModelProperty(value = "数据记录数")
    private Integer dataCount;

    /**
     * 服务类型
     */
    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}