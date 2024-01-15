package com.fisk.datamodel.dto.fact;

import lombok.Data;

import java.util.List;

@Data
public class FactTreeDTO {

    //按应用区分的事实表
    private List<BusinessAreaFactDTO> factByArea;

}
