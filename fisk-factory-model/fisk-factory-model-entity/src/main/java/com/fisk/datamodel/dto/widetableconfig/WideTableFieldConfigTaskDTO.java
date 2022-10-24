package com.fisk.datamodel.dto.widetableconfig;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceTableConfigDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableFieldConfigTaskDTO extends MQBaseDTO {

    public int id;

    public int businessId;

    public String name;

    public String sqlScript;

    public Long userId;

    public OlapTableEnum wideTable;

    public List<TableSourceTableConfigDTO> entity;

    public List<TableSourceRelationsDTO> relations;

}
