package com.fisk.dataservice.dto.api;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-03-19
 * @Description:
 */
@Data
public class FieldEncryptConfigDTO {
    /**
     * apiId
     */
    public Integer apiId;
    /**
     * 加密key
     */
    public String encryptKey;

    public List<FieldEncryptDTO> fieldEncryptDTOS;
}
