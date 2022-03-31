package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_access")
public class TableAccessPO extends BasePO {

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * 父id
     */
    public int pid;

    /**
     * tb_app_registration表id
     */
    public Long appId;

    /**
     * apiId
     */
    public Long apiId;


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
     * 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public Integer publish;

    /**
     * SQL脚本or文件全限定名称
     */
    public String sqlScript;

    /**
     * excel sheet页名称
     */
    public String sheet;
}
