package com.fisk.system.dto.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 测试连接 DTO
 * @date 2022/6/13 14:51
 */
@Data
public class TestConnectionDTO {
    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串")
    @Length(min = 0, max = 500, message = "长度最多500")
    @NotNull()
    public String conStr;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    @NotNull
    public DataSourceTypeEnum conType;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String conAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String conPassword;
}
