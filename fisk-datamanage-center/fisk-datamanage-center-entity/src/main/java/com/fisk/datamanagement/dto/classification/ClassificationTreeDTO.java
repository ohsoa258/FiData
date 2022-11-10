package com.fisk.datamanagement.dto.classification;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationTreeDTO {

    public String guid;

    public String name;

    public List<ClassificationTreeDTO> child;

}
