package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
@TableName(value = "tb_database_account")
public class DataViewAccountPO extends BasePO {

    @ApiModelProperty(value= "数据视图id")
    private Integer viewThemeId ;

    @ApiModelProperty(value= "数据库账号名称")
    private String accountName ;

    @ApiModelProperty(value = "数据库账号描述")
    private String accountDesc ;

    @ApiModelProperty(value = "数据库账号密码")
    private String accountPsd ;

    @ApiModelProperty(value = "数据库账号权限")
    private String jurisdiction ;
}
