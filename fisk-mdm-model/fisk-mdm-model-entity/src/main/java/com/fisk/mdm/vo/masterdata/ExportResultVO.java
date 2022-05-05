package com.fisk.mdm.vo.masterdata;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022/5/5 14:17
 */
@Data
public class ExportResultVO {

    /**
     * 导出文件名
     */
    public String fileName;
    /**
     * 查询数据集
     */
    public JSONArray dataArray;
    /**
     * 标头集合
     */
    public List<String> headerList;

}
