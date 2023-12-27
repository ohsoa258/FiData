package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joda.time.DateTime;

import java.time.LocalDateTime;

/**
 * @author xgf
 * @date 2023年11月22日 16:49
 */
@Data
@TableName("tb_business_extendedfields")
public class BusinessExtendedfieldsPO {
    public String  dimdomaintype;
    public String  dimdomainid;
    public String  dimdomain;
    public String  dimtableid;
    public String  dimtable;
    public String  attributeid;
    public String  attribute;
    public  String indexid;
    public String createdUser;
//    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public String createdTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    public LocalDateTime updateTime;

    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    public String updateUser;

    @TableLogic
    public int delFlag;

}
