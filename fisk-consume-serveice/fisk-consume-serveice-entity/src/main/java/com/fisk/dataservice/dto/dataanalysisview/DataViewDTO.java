package com.fisk.dataservice.dto.dataanalysisview;

import com.fisk.common.core.baseObject.dto.BaseDTO;
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
@ApiModel(value = "DataViewDTO", description = "数据视图DTO")
public class DataViewDTO extends BaseDTO {

    @ApiModelProperty(value = "数据视图id")
    private Integer id ;

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
