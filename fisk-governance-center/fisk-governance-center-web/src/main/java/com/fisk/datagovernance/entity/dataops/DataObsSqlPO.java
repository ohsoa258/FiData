package com.fisk.datagovernance.entity.dataops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-01-03 09:49:11
 */
@TableName("tb_data_obs_sql")
@Data
public class DataObsSqlPO extends BasePO {

    @ApiModelProperty(value = "tab名称")
    private String tabName;

    @ApiModelProperty(value = "querySql")
    private String querySql;

    @ApiModelProperty(value = "数据库id")
    private String dbId;

}
