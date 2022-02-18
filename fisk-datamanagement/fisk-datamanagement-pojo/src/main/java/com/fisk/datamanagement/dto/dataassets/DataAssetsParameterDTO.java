package com.fisk.datamanagement.dto.dataassets;

import com.fisk.common.filter.dto.FilterQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataAssetsParameterDTO {

    /**
     * 实例guid
     */
    public String instanceGuid;
    /**
     * 数据库名称
     */
    public String dbName;
    /**
     * 表名称
     */
    public String tableName;
    /**
     * 字段名称
     */
    public String columnName;
    /**
     * 当前页
     */
    public int pageIndex;
    /**
     * 每页条数
     */
    public int pageSize;
    /**
     * 筛选条件
     */
    public List<FilterQueryDTO> filterQueryDTOList;
    /**
     * 是否导出
     */
    public boolean export;

}
