package com.fisk.dataaccess.dto.v3;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TbTableAccessDTO {
    public long id;

    /**
     * tb_app_registration表id
     */
    public Long appId;

    /**
     * 应用名称
     */
    public String appName;
    /**
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    public Integer isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    public Integer publish;
    /**
     * SQL脚本
     */
    public String sqlScript;

    /**
     * 0: 发布;  1: 保存sql脚本
     */
    public int flag;

    /**
     * 用于拦截sql保存时空字符
     */
    public int sqlFlag;
}
