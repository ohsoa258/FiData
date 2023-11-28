package com.fisk.dataservice.dto.ksfwebservice.notice;

import com.alibaba.fastjson.annotation.JSONField;
import com.fisk.dataservice.dto.ksfwebservice.item.KsfGoods;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-27
 * @Description:
 */
public class Data {
    @JSONField(name = "DocCount")
    private int DocCount;
    @JSONField(name = "Ksf_Notice")
    private List<KsfNotice> ksfNotices;

    public int getDocCount() {
        return DocCount;
    }

    public void setDocCount(int docCount) {
        DocCount = docCount;
    }

    public List<KsfNotice> getKsfNotices() {
        return ksfNotices;
    }

    public void setKsfNotices(List<KsfNotice> ksfNotices) {
        this.ksfNotices = ksfNotices;
    }
}
