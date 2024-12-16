package com.fisk.datagovernance.dto.dataquality.datacheck.apidoc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 联系人
 * @date 2022/2/3 14:13
 */
@Data
public class ApiContactsDTO {
    /**
     * 类别
     */
    @ApiModelProperty(value = "类别")
    public String category;

    /**
     * 所属公司/部门
     */
    @ApiModelProperty(value = "所属公司/部门")
    public String company;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    public String fullName;

    /**
     * 联系方式，邮箱地址
     */
    @ApiModelProperty(value = "联系方式，邮箱地址")
    public String mailbox;

    /**
     * 行样式
     */
    @ApiModelProperty(value = "行样式")
    public String trStyle;
}
