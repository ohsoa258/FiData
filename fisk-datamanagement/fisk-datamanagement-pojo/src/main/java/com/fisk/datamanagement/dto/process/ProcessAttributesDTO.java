package com.fisk.datamanagement.dto.process;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProcessAttributesDTO {

    public String qualifiedName;

    public String name;

    public String description;

    public List<ProcessAttributesPutDTO> outputs;

    public List<ProcessAttributesPutDTO> inputs;

}
