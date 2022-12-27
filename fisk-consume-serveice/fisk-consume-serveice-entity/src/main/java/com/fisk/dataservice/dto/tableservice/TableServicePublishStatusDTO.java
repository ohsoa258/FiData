package com.fisk.dataservice.dto.tableservice;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServicePublishStatusDTO {

    /**
     * 表服务id
     */
    public int id;
    /**
     * 发布状态:0: 未发布  1: 发布成功  2: 发布失败
     */
    public int status;

}
