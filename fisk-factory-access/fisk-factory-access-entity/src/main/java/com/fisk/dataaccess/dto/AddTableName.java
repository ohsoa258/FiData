package com.fisk.dataaccess.dto;

import com.fisk.common.core.response.ResultEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AddTableName {

    public ResultEnum code;
    public String tableName;
}
