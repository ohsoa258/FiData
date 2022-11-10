package com.fisk.common.service.dbBEBuild.common.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DruidFieldInfoDTO {

    public String fieldName;

    public String tableName;

    public boolean alias;

    public String logic;

}
