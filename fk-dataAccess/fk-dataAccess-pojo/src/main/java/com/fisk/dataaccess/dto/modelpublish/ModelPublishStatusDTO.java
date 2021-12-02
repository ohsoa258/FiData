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
     * 发布状态
     */
    public int publish;
}