package com.fisk.dataservice.dto.ksfwebservice.item;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @Author: wangjian
 * 物料主数据
 * @Date: 2023-10-24
 * @Description:
 */

public class ItemData {
    @JSONField(name = "SourceSys")
    private String SourceSys;
    @JSONField(name = "TargetSys")
    private String TargetSys;
    @JSONField(name = "PushSeqNo")
    private Integer PushSeqNo;
    @JSONField(name = "WMSID")
    private String WMSID;
    @JSONField(name = "Data")
    private Data Data;

    public String getSourceSys() {
        return SourceSys;
    }

    public void setSourceSys(String sourceSys) {
        SourceSys = sourceSys;
    }

    public String getTargetSys() {
        return TargetSys;
    }

    public void setTargetSys(String targetSys) {
        TargetSys = targetSys;
    }

    public Integer getPushSeqNo() {
        return PushSeqNo;
    }

    public void setPushSeqNo(Integer pushSeqNo) {
        PushSeqNo = pushSeqNo;
    }

    public String getWMSID() {
        return WMSID;
    }

    public void setWMSID(String WMSID) {
        this.WMSID = WMSID;
    }

    public Data getData() {
        return Data;
    }

    public void setData(Data data) {
        Data = data;
    }
}