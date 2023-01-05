package com.fisk.system.vo.license;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 许可证VO
 * @date 2023/1/5 10:43
 */
@Data
public class LicenceVO {

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
    @ApiModelProperty(value = "客户许可证")
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
     * 秘钥授权时间，年/月/日格式
     */
    @ApiModelProperty(value = "秘钥授权时间，年/月/日格式")
    public String authorizationDate;

    /**
     * 许可证状态
     */
    @ApiModelProperty(value = "许可证状态")
    public String licenseState;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
