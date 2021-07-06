package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_area_data")
@EqualsAndHashCode(callSuper = true)
public class DataAreaPO extends BaseEntity {

    @TableId
    public long id;

    /**
     *  业务域表id
     */
    public long businessid;

    /**
     *  数据域名称
     */
    public String dataName;

    /**
     *  1true  0false
     */
    public boolean share;

    /**
     *  数据域描述
     */
    public String dataDes;

//    public LocalDateTime createTime;

    public String createUser;

//    public LocalDateTime updateTime;

    public String updateUser;

    public int delFlag;

}
