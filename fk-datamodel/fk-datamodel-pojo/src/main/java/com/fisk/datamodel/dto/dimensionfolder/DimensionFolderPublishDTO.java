package com.fisk.datamodel.dto.dimensionfolder;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderPublishDTO {
    public long dimensionId;
    public String dimensionName;
    public String sqlScript;
    public List<DimensionFolderPublishDetailDTO> fieldList;
}
