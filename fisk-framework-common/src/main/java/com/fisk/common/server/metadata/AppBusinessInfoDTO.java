package com.fisk.common.server.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JianWenYang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppBusinessInfoDTO {

    public long id;

    public String name;

    public String appAbbreviation;

    public String appDes;

    public Integer sourceType;

}
