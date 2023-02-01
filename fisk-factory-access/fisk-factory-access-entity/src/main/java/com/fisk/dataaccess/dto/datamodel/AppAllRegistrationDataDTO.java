package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SongJianJian
 */
@Data
public class AppAllRegistrationDataDTO {

    private long id;

    private String name;

    public List<AppRegistrationDataDTO> appList = new ArrayList<>();

}
