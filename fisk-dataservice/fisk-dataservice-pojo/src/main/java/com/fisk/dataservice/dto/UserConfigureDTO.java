package com.fisk.dataservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/4 10:12
 */
@Data
public class UserConfigureDTO {
    public Long id;
    public List<String> apiName;
}
