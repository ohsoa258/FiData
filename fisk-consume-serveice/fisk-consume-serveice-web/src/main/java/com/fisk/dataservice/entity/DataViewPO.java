package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "tb_view")
public class DataViewPO extends BasePO {

    @ApiModelProperty(value= "所属视图主题id")
    private Integer viewThemeId ;

    @ApiModelProperty(value = "视图名称")
    private String name ;

    @ApiModelProperty(value = "视图显示名称")
    private String displayName ;

    @ApiModelProperty(value = "视图描述")
    private String viewDesc ;

    @ApiModelProperty(value = "视图脚本语句")
    private String viewScript ;
}
