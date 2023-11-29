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
 * @date 2023-11-20 13:56:24
 */
@TableName("tb_standards_menu")
@Data
public class StandardsMenuPO extends BasePO {

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "类型:1:目录 2:数据")
    private Integer type;

    @ApiModelProperty(value = "排序")
    private Integer sort;
}
