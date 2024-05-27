package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-05-13 14:04:19
 */
@TableName("tb_datacheck_code")
@Data
public class DatacheckCodePO extends BasePO {
    @ApiModelProperty(value = "code名称")
    private String codeName;
    @ApiModelProperty(value = "code值")
    private String codeValue;

}
