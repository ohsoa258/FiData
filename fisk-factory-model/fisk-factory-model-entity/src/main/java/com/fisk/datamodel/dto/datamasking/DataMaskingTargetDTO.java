package com.fisk.datamodel.dto.datamasking;

import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description 数据脱敏出参
 * @date 2022/4/15 14:03
 */
@Data
public class DataMaskingTargetDTO {

    public String url;

    public String username;

    public String password;

    public String tableName;
}
