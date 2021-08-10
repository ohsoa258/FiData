package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * @author cfk
 */
@Data
@TableName("tb_business_limited")
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLimitedPO {
    public int id;
    //业务限定名称
    public String limitedName;
    //业务限定描述
    public String limitedDes;
    //创建时间
    public Date createTime;
    //创建人
    public String createUser;
    //修改时间
    public Date updateTime;
    //修改人
    public String updateUser;
    //是否删除
    public int delFlag;
    //事实表id
    public int factId;
}
