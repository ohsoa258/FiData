package com.fisk.datamodel.dto.businessprocess;

import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-08-07
 * @Description:
 */
@Data
public class BusinessQueryDataParamDTO {
    private String dbName;
    private String ip;
    private List<FactAttributeDTO> factAttributeDTOList;
}
