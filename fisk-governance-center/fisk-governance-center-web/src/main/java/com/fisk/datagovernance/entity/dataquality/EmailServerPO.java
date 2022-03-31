package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置表
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_emailserver_config")
public class EmailServerPO extends BasePO {
    /**
     * 名称
     */
    @TableField("`name`")
    public String name;

    /**
     * 邮件服务器
     */
    public String emailServer;

    /**
     * 邮件服务器端口
     */
    public int emailServerPort;

    /**
     * 发件账号
     */
    public String emailServerAccount;

    /**
     * 发件密码
     */
    public String emailServerPwd;

    /**
     * 邮件服务器类型
     */
    public int emailServerType;

    /**
     * 是否启用SSL加密连接
     */
    public Integer enableSsl;
}
