package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AppRegistrationDataDTO {

    public long id;
    public String appName;
    public String appAbbreviation;
    public boolean whetherSchema;
    public Integer targetDbId;
    public List<TableAccessDataDTO> tableDtoList;

}
