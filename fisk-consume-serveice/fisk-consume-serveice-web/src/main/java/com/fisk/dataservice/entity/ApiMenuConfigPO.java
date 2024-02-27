package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-01-26 14:46:30
 */
@TableName("tb_api_menu_config")
@Data
public class ApiMenuConfigPO extends BasePO {

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "类型:1:目录 2:数据")
    private Integer type;

    @ApiModelProperty(value = "排序字段")
    private Integer sort;

    @ApiModelProperty(value = "服务类型：1.本地服务 2.代理服务")
    private Integer serverType;
}
