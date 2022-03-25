package com.fisk.datagovernance.vo.dataquality.emailserver;

import com.fisk.datagovernance.enums.dataquality.EmailServerTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置
 * @date 2022/3/22 15:37
 */
@Data
public class EmailServerVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;

    /**
     * 邮件服务器
     */
    @ApiModelProperty(value = "邮件服务器")
    public String emailServer;

    /**
     * 邮件服务器端口
     */
    @ApiModelProperty(value = "邮件服务器端口")
    public int emailServerPort;

    /**
     * 发件账号
     */
    @ApiModelProperty(value = "发件账号")
    public String emailServerAccount;

    /**
     * 发件密码
     */
    @ApiModelProperty(value = "发件密码")
    public String emailServerPwd;

    /**
     * 邮件服务器类型
     */
    @ApiModelProperty(value = "邮件服务器类型")
    public EmailServerTypeEnum emailServerType;

    /**
     * 是否启用SSL加密连接
     */
    @ApiModelProperty(value = "是否启用SSL加密连接")
    public Integer enableSsl;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
