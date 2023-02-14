package com.fisk.datamanagement.dto.search;

import com.fisk.datamanagement.dto.classification.ClassificationDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.label.LabelDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class EntitiesDTO {

    public String displayText;

    public List<ClassificationDTO> classifications;

    public List<String> classificationNames;

    public List<String> meaningNames;

    public String typeName;

    public String guid;

    public EntityAttributesDTO attributes;

    public List<GlossaryDTO> meanings;

    public String status;

    public String isIncomplete;

    public List<LabelDTO> labels;

}