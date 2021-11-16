package com.fisk.datamodel.dto.dimensionfolder;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderPublishQueryDTO {
    public int businessAreaId;
    public List<Integer> dimensionFolderIds;
    public int dimensionId;
}
