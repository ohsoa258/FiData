package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AppRegistrationDataDTO {

    public long id;
    public String appName;
    public String appAbbreviation;
    public List<TableAccessDataDTO> tableDtoList;

}
