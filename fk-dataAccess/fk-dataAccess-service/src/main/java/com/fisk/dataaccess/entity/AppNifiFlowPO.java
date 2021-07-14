package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@TableName("tb_app_nififlow")
public class AppNifiFlowPO implements Serializable {

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
