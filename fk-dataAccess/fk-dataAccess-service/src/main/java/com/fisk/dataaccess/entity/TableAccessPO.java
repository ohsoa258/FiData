package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_access")
public class TableAccessPO extends BaseEntity {

    @TableId
    public long id;

    /**
     *  tb_app_registration表id
     */
    public long appid;

    /**
     * 添加数据时后台生成
     */
    public String atlasTableId;

    /**
     * 组件id
     */
    public String componentId;

    /**
     * nifi sql
     */
    public String dorisSelectSqlStr;

    /**
     *  物理表名
     */
    public String tableName;

    /**
     *  物理表描述
     */
    public String tableDes;

    /**
     *  如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     *  0是实时物理表，1是非实时物理表
     */
    public int isRealtime;

//    public LocalDateTime createTime;

    public String createUser;

//    public LocalDateTime updateTime;

    public String updateUser;

    public int delFlag;

}
