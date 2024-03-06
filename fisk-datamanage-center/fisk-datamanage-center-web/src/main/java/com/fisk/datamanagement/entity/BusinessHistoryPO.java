package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-03-01 14:39:17
 */
@TableName("tb_business_history")
@Data
public class BusinessHistoryPO extends BasePO {
    @ApiModelProperty(value = "")
    private Integer targetinfoId;
    @ApiModelProperty(value = "")
    private String historyId;

}
