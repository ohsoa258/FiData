package com.fisk.dataaccess.dto.apicondition;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionInfoDTO {

    public String typeName;

    public String parentTypeName;

    public List<ApiConditionDetailDTO> data;

    public List<ApiConditionInfoDTO> child;

}
