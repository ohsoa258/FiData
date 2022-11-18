package com.fisk.datamanagement.dto.dataassets;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataAssetsResultDTO {
    /**
     * 查询数据集
     */
    public JSONArray dataArray;

    public List<String[]> columnList;

    public int pageIndex;

    public int pageSize;

    public int total;
}
