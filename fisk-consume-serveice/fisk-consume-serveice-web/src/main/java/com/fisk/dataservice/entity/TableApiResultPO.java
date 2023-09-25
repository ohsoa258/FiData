package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 * @date 2023-09-08 13:38:07
 */
@Data
@TableName("tb_table_api_result")
public class TableApiResultPO extends BasePO {

    @ApiModelProperty(value = "app_id")
    private Integer appId;

    @ApiModelProperty(value = "字段名称")
    private String name;

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "1:选中0:不选")
    private int selected;
}
