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
@TableName("tb_view_fields")
public class ViewFieldsPO extends BasePO {
    @ApiModelProperty(name = "",notes = "字段所属视图id")
    private Integer viewId ;

    @ApiModelProperty(name = "",notes = "字段名称")
    private String fieldName ;
    /** 字段类型 */
    @ApiModelProperty(name = "",notes = "字段类型")
    private String fieldType ;
    /** 字段描述 */
    @ApiModelProperty(name = "",notes = "字段描述")
    private String fieldDesc ;
    /** 字段长度 */
    @ApiModelProperty(name = "",notes = "字段长度")
    private String fieldLength ;
    /** 字段精度 */
    @ApiModelProperty(name = "",notes = "字段精度")
    private Integer fieldPrecision ;
    /** 显示名称 */
    @ApiModelProperty(name = "",notes = "显示名称")
    private String showName ;
    /** 数据类型 */
    @ApiModelProperty(name = "",notes = "数据类型")
    private String dataType ;

}
