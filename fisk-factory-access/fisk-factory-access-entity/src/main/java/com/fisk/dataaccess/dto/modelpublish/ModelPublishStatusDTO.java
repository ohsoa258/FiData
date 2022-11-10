package com.fisk.dataaccess.dto.modelpublish;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author yoyo
 */
@Data
public class ModelPublishStatusDTO {
    /**
     * api_id
     */
    public Long apiId;
    /**
     * 物理表id
     */
    @NotNull
    public long tableId;
    /**
     * 发布状态 1: 发布成功  2: 发布失败
     */
    public int publish;
    /**
     * 发布错误信息
     */
    public String publishErrorMsg;
}