package com.fisk.dataservice.dto.datasource;

import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceInfoDTO {

    public Integer dbId;

    public String dbName;

    public List<TableNameDTO> tableNameList;

}
