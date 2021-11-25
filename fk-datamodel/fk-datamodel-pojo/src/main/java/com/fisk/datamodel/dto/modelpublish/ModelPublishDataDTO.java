package com.fisk.datamodel.dto.modelpublish;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishDataDTO {
    public long businessAreaId;
    public String businessAreaName;
    public long userId;
    public int createType;
    public List<ModelPublishTableDTO> dimensionList;
}
