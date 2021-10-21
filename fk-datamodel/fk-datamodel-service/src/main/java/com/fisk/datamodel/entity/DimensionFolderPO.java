package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_dimension_folder")
@Data
@EqualsAndHashCode(callSuper = true)
public class DimensionFolderPO extends BasePO {

    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 维度文件夹中文名称
     */
    public String dimensionFolderCnName;
    /**
     * 维度文件夹英文名称
     */
    public String dimensionFolderEnName;
    /**
     * 维度文件夹描述
     */
    public String dimensionFolderDesc;
    /**
     * 是否共享
     */
    public boolean share;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    public int isPublish;

}
