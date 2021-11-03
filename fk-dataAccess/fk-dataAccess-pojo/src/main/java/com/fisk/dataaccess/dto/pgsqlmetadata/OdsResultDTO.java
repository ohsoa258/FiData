package com.fisk.dataaccess.dto.pgsqlmetadata;

import com.alibaba.fastjson.JSONArray;
import com.fisk.dataaccess.dto.FieldNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class OdsResultDTO {
    /**
     * 查询数据集
     */
    public JSONArray dataArray;
    /**
     * 表字段集合
     */
    public List<FieldNameDTO> fieldNameDTOList;

    public int pageIndex;

    public int pageSize;

    public int total;
}
