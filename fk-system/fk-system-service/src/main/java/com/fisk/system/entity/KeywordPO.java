package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lock
 */
@Data
@TableName("tb_keyword")
public class KeywordPO implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * SQL关键词
     */
    public String keyword;

    /**
     * SQL关键词类型(1:mysql  2:sqlserver  3:pgsql  4:doris)
     */
    public long keywordType;

    /**
     * 描述
     */
    public String describe;
}
