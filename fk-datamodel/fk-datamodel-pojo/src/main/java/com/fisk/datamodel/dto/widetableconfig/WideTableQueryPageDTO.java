package com.fisk.datamodel.dto.widetableconfig;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableQueryPageDTO {
    /**
     * 查询数据集
     */
    public JSONArray dataArray;

    public List<String> columnList;

    public int pageSize;

}
