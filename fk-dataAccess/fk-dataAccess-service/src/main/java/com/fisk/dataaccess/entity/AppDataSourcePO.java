package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_app_datasource")
public class AppDataSourcePO extends BasePO {
//    /**
//     * 主键
//     */
//    @TableId
//    private long id;

    /**
     * tb_app_registration表id
     */
    private long appid;

    /**
     * tb_app_drivetype表type
     */
    public String driveType;

    /**
     * 主机名
     */
    public String host;

    /**
     * 端口号
     */
    public String port;

    /**
     * 数据库名
     */
    public String dbName;

    /**
     * 添加数据时后台生成
     */
    public String atlasDbId;

    /**
     * 数据源连接字符串
     */
    public String connectStr;

    /**
     * 连接账号
     */
    public String connectAccount;

    /**
     * 连接密码
     */
    public String connectPwd;

    /**
     * 验证方式（实时） 登录账号
     */
    public String realtimeAccount;

    /**
     * 验证方式（实时） 登录密码
     */
    public String realtimePwd;


//    /**
//     * 创建人(id)
//     */
//    public String createUser;
//
//    /**
//     * 更新人(id)
//     */
//    public String updateUser;
//
//    /**
//     * 逻辑删除(1: 未删除; 0: 删除)
//     */
//    public int delFlag;
}
