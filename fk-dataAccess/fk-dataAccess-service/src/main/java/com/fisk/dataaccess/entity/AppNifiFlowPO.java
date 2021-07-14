package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_app_nififlow")
@EqualsAndHashCode(callSuper = true)
public class AppNifiFlowPO extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.INPUT)
    public long id;

    /**
     * nifi所需sql
     */
    public String dorisSelectSqlStr;

}
