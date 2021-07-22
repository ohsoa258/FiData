package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_project_dimension")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDimensionPO extends BasePO {

    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 数据域id
     */
    public int dataId;
    /**
     * 项目id
     */
    public String projectId;
    /**
     * 维度名称
     */
    public String dimensionCnName;
    /**
     * 维度英文名称
     */
    public String dimensionEnName;
    /**
     * 维度逻辑表名称
     */
    public String dimensionTabName;
    /**
     * 维度描述
     */
    public String dimensionDesc;
}
