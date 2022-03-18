package com.fisk.datamodel.dto.widetableconfig;

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

    public String sql;

    public Long userId;

    public OlapTableEnum wideTable;

    public List<WideTableSourceTableConfigDTO> entity;

    public List<WideTableSourceRelationsDTO> relations;

}
