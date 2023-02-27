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
@TableName(value = "tb_database_role")
public class DataViewRolePO extends BasePO {

    @ApiModelProperty(name = "",notes = "")
    private String dbName ;
    /**  */
    @ApiModelProperty(name = "",notes = "")
    private String roleName ;

    private Integer themeId;
}
