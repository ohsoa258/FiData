package com.fisk.dataservice.dto.dataanalysisview;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "DataViewAccountDTO", description = "数据视图关联账号DTO")
public class DataViewAccountDTO extends BaseDTO {

    @ApiModelProperty(value = "账号id")
    private Integer id;

    @ApiModelProperty(value= "数据视图id")
    private Integer viewThemeId ;

    @ApiModelProperty(value= "数据库账号名称", required = true)
    @NotEmpty(message = "账号名称不能为空")
    private String accountName ;

    @ApiModelProperty(value = "数据库账号描述", required = true)
    @NotEmpty(message = "账号描述不能为空")
    private String accountDesc ;

    @ApiModelProperty(value = "数据库账号密码")
    @NotEmpty(message = "账号密码不能为空")
    private String accountPsd ;

    @ApiModelProperty(value = "数据库账号权限")
    private String jurisdiction ;
}
