package com.fisk.datamodel.dto.dimensionfolder;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderPublishDataDTO {
    public long businessAreaId;
    public String businessAreaName;
    public long userId;
    public int createType;
    public List<DimensionFolderPublishDTO> dimensionList;
}
