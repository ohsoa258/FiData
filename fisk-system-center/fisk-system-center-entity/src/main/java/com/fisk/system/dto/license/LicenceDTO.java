package com.fisk.system.dto.license;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 许可证DTO
 * @date 2022/11/10 15:24
 */
@Data
public class LicenceDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 客户编号
     */
    @ApiModelProperty(value = "客户编号")
    public String customerCode;

    /**
     * 客户名称
     */
    @ApiModelProperty(value = "客户名称")
    public String customerName;

    /**
     * 客户密钥
     */
    @ApiModelProperty(value = "客户密钥")
    public String customerLicense;

    /**
     * 机器密钥
     */
    @ApiModelProperty(value = "机器密钥")
    public String machineKey;

    /**
     * 服务范围，菜单ID逗号分隔
     */
    @ApiModelProperty(value = "服务范围，菜单ID逗号分隔")
    public String servicesScope;

    /**
     * 秘钥到期时间，年/月/日格式
     */
    @ApiModelProperty(value = "秘钥到期时间，年/月/日格式")
    public String expirationDate;

    /**
     * 菜单集合
     */
    @ApiModelProperty(value = "菜单集合")
    public List<MenuDTO> menuList;
}
