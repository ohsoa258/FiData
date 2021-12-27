package com.fisk.datamanagement.dto.classification;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationAddEntityDTO {
    public ClassificationDTO classification;
    public List<String> entityGuids;
}
