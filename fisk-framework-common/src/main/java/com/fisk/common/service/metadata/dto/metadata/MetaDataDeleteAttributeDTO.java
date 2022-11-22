package com.fisk.common.service.metadata.dto.metadata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-07 16:13
 */
@Data
public class MetaDataDeleteAttributeDTO {

    public List<String> qualifiedNames;

    /**
     * atlas业务名称
     */
    public String classifications;

}
