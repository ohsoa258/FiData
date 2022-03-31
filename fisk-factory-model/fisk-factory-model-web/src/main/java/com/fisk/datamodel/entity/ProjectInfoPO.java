package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_project_info")
@EqualsAndHashCode(callSuper = true)
public class ProjectInfoPO extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    public long id;

    /**
     * tb_area_business(id)
     */
    public long businessId;

    /**
     * 项目空间模式
     */
    public String projectPattern;

    /**
     * 项目名称
     */
    public String projectName;

    /**
     * 项目描述
     */
    public String projectDes;

    /**
     * 项目负责人
     */
    public String projectPrincipal;

    /**
     * 负责人邮箱
     */
    public String principalEmail;

    /**
     * 项目状态(0未发布,1发布)
     */
    public int projectFlag;


    /**
     * 创建人
     */
    public String createUser;

    /**
     * 更新人
     */
    public String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    public int delFlag;

}
