package com.fisk.dataaccess.dto.json;

import com.alibaba.fastjson.JSONArray;
import lombok.Builder;
import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description 表数据
 * @date 2022/1/20 14:28
 */
@Data
@Builder
public class JsonTableData {
    public String table;
    public JSONArray data;
}
