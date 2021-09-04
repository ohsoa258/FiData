package com.fisk.datamodel.dto.metadatakinship;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class InstanceDTO {

    public String value;
    public String name;
    public List<DbBaseDTO> list;
}
