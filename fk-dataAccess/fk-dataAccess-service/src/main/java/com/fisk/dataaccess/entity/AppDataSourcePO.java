package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * @author: Lock
 * @data: 2021/5/26 14:20
 */
@Data
@TableName("tb_app_datasource")
public class AppDataSourcePO extends BaseEntity {
    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * tb_app_registration表id
     */
    private String appId;

    /**
     * tb_app_drivetype表type
     */
    private String driveType;

    /**
     * 数据源连接字符串
     */
    private String connectStr;

    /**
     * 连接账号
     */
    private String connectAccount;

    /**
     * 连接密码
     */
    private String connectPwd;

    /**
     * 验证方式（实时） 登录账号
     */
    private String realtimeAccount;

    /**
     * 验证方式（实时） 登录密码
     */
    private String realtimePwd;

    /**
     * 创建时间
     */
//    private DateTime createTime;

    /**
     * 创建人(id)
     */
    private String createUser;

    /**
     * 更新时间
     */
//    private DateTime updateTime;

    /**
     * 更新人(id)
     */
    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    private byte delFlag;
}
