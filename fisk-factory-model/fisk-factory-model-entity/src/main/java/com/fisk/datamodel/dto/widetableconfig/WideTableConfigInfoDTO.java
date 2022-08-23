package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.datamodel.dto.widetablefieldconfig.WideTableFieldConfigsDTO;
import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableConfigInfoDTO {

    public int pageSize;

    public List<WideTableSourceTableConfigDTO> entity;

    public List<WideTableRelationConfigDTO> relations;

}
