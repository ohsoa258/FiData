package com.fisk.datamanagement.dto.metadatalabelmap;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class MetadataLabelMapParameter {

    public Integer metadataEntityId;

    public List<Integer> labelIds;

}
