package com.fisk.datamodel.dto.businessprocess;

import com.fisk.datamodel.dto.atomicindicator.IndicatorsDataDTO;
import com.fisk.datamodel.dto.fact.FactDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessListDTO {

    public long id;
    /**
     * 业务过程中文名称
     */
    public String businessProcessCnName;
    /**
     * 业务过程下事实列表
     */
    public List<FactDataDTO> factList;

}
