package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName tq_data_encryption_config
 */
@TableName(value = "tq_data_encryption_config")
@Data
public class TqDataEncryptionConfigPO extends BasePO implements Serializable {

    /**
     * 接口名称
     */
    @ApiModelProperty(value = "接口名称")
    private String apiName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    /**
     * 分级
     */
    @ApiModelProperty(value = "分级")
    private String level;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类")
    private String type;

    /**
     * 是否加密 0否 1是
     */
    @ApiModelProperty(value = "是否加密 0否 1是")
    private Integer isEncrypted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}