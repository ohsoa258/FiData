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
 * @TableName tq_log_storage
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="tq_log_storage")
@Data
public class TqLogStoragePO extends BasePO implements Serializable {

    /**
     * 服务名称
     */
    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    /**
     * 清理日志大小
     */
    @ApiModelProperty(value = "清理日志大小")
    private String clearSize;

    /**
     * 备份日志大小
     */
    @ApiModelProperty(value = "备份日志大小")
    private String backupSize;

    /**
     * 收集时间
     */
    @ApiModelProperty(value = "收集时间")
    private String collectTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}