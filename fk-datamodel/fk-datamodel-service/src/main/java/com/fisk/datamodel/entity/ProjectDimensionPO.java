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
    /**
     * 表来源
     */
    public String tableSource;
    /**
     *数据来源(1:数据接入表,2:sql自定义字段，3:维度表)
     */
    public int dataSource;
    /**
     * 维度表类型(1:业务主键,2:关联维度，3:属性)
     */
    public int dimensionTabType;

}
