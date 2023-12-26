package com.fisk.common.service.accessAndModel;

import lombok.Data;

import java.util.List;

@Data
public class AccessAndModelTreeDTO {

    /**
     * 数接树
     */
    private List<AccessAndModelAppDTO> accessTree;

    /**
     * 数仓树
     */
    private List<AccessAndModelAppDTO> modelTree;

}
