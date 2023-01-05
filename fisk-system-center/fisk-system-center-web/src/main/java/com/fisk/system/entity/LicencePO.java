package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description licence许可证管理
 * @date 2023/1/4 16:05
 */
@Data
@TableName("tb_system_licence")
public class LicencePO extends BasePO {

    /**
     * 客户Code
     */
    public String customerCode;

    /**
     * 客户名称
     */
    public String customerName;

    /**
     * 客户密钥
     */
    public String customerLicense;

    /**
     * 机器密钥
     */
    public String machineKey;

    /**
     * 服务范围，菜单ID逗号分隔
     */
    public String servicesScope;

    /**
     * 秘钥到期时间，年-月-日格式
     */
    public String expirationDate;
}
