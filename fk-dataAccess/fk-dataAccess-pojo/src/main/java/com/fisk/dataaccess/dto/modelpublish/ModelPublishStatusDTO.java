package com.fisk.dataaccess.dto.modelpublish;

import lombok.Data;

/**
 * @author yoyo
 */
@Data
public class ModelPublishStatusDTO {
    /**
     * 物理表id
     */
    public long tableId;
    /**
     * 发布状态 1: 发布成功  2: 发布失败
     */
    public int publish;
}