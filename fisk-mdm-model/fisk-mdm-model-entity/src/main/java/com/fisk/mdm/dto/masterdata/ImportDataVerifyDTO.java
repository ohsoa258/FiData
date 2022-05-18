package com.fisk.mdm.dto.masterdata;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ImportDataVerifyDTO {

    /**
     * 是否成功
     */
    private Boolean success;

    private String value;

    private String errorMsg;

}
