package com.fisk.datamodel.dto.modelpublish;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishStatusDTO {
    /**
     * 维度id/事实id
     */
    public int id;
    /**
     * 发布状态
     */
    public int status;
    /**
     * 类型：0 DW、1 Doris
     */
    public int type;
}
