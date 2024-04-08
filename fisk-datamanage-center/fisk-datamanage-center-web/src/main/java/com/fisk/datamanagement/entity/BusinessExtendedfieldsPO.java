package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xgf
 * @date 2023年11月22日 16:49
 */
@Data
@TableName("tb_business_extendedfields")
public class BusinessExtendedfieldsPO {
    @TableId(value = "id", type = IdType.AUTO)
    public int id;
    public String dimdomainid;
    public String dimtableid;
    public String attributeid;
    public String indexid;
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
