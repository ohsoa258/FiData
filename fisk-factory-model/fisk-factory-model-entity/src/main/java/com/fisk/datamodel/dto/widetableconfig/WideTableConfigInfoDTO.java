package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableConfigInfoDTO {

    public int pageSize;

    public List<TableSourceTableConfigDTO> entity;

    public List<WideTableRelationConfigDTO> relations;

}
