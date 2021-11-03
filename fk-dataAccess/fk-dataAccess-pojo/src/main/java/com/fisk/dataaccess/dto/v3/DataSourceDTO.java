package com.fisk.dataaccess.dto.v3;

import com.fisk.dataaccess.dto.TablePyhNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DataSourceDTO {

    public long id;
    /**
     * 数据源
     */
    public String driveType;
    /**
     * 数据库名
     */
    public String databaseName;
    /**
     * 表
     */
    public List<TablePyhNameDTO> list;
}
