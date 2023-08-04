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

    public Integer appType;

    public AppBusinessInfoDTO(Long id, String name, String appAbbreviation, String appDes, Integer sourceType) {
        this.setId(id);
        this.setName(name);
        this.setAppAbbreviation(appAbbreviation);
        this.setAppDes(appDes);
        this.setSourceType(sourceType);
    }

}
