package com.fisk.dataservice.dto.ksfwebservice.item;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-27
 * @Description:
 */
public class Data {
    @JSONField(name = "DocCount")
    private int DocCount;
    @JSONField(name = "KsfGoods")
    private List<KsfGoods> KsfGoods;

    public int getDocCount() {
        return DocCount;
    }

    public void setDocCount(int docCount) {
        DocCount = docCount;
    }

    public List<KsfGoods> getKsfGoods() {
        return KsfGoods;
    }

    public void setKsfGoods(List<KsfGoods> ksfGoods) {
        KsfGoods = ksfGoods;
    }
}
