package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tq_views_permissions_config
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_views_permissions_config")
@Data
public class TqViewsPermissionsConfigPO extends BasePO implements Serializable {

    /**
     * 接口名称
     */
    @ApiModelProperty(value = "接口名称")
    private String apiName;

    /**
     * 接口标识
     */
    @ApiModelProperty(value = "接口标识")
    private String apiCode;

    /**
     * 场景名称
     */
    @ApiModelProperty(value = "场景名称")
    private String sceneName;

    /**
     * 是否开启权限 0否1是
     */
    @ApiModelProperty(value = "是否开启权限 0否1是")
    private Integer isOpen;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}