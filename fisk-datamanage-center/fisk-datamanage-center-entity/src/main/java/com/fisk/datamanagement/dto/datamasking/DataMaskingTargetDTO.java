package com.fisk.datamanagement.dto.datamasking;

import lombok.Data;

/**
 * @author JianWenYang
 * @description 数据脱敏出参dto
 * @date 2022/4/15 14:00
 */
@Data
public class DataMaskingTargetDTO {

    public String url;

    public String username;

    public String password;

    public String tableName;
}
