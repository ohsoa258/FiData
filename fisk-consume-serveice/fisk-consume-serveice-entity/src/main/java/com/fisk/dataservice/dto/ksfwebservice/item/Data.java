package com.fisk.dataservice.dto.ksfwebservice.item;

import com.alibaba.fastjson.annotation.JSONField;
import com.fisk.dataservice.dto.ksfwebservice.Inventory.MATDOCTAB;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-27
 * @Description:
 */
public class Data {
    @JSONField(name = "DocCount")
    private int DocCount;
    @JSONField(name = "MATDOCTAB")
    private List<com.fisk.dataservice.dto.ksfwebservice.Inventory.MATDOCTAB> MATDOCTAB;

    public int getDocCount() {
        return DocCount;
    }

    public void setDocCount(int docCount) {
        DocCount = docCount;
    }

    public List<MATDOCTAB> getMATDOCTAB() {
        return MATDOCTAB;
    }

    public void setMATDOCTAB(List<MATDOCTAB> MATDOCTAB) {
        this.MATDOCTAB = MATDOCTAB;
    }
}
