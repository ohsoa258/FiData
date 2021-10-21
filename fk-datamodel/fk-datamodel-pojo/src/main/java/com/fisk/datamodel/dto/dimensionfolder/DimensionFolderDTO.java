package com.fisk.datamodel.dto.dimensionfolder;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderDTO {

    public long id;
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

}
