package com.fisk.datagovern.dto.category;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDataDTO extends CategoryDTO {
    public List<CategoryDataDTO> childrenDto;
}
