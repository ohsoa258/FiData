package com.fisk.dataservice.dto.ksfwebservice.notice;

import com.alibaba.fastjson.annotation.JSONField;
import com.fisk.dataservice.dto.ksfwebservice.item.Data;

/**
 * @Author: wangjian
 * 通知单
 * @Date: 2023-10-24
 * @Description:
 */

public class NoticeData {
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

}
