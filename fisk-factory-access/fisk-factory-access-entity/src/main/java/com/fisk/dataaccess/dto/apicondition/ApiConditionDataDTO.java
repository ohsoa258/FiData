package com.fisk.dataaccess.dto.apicondition;

import com.fisk.dataaccess.enums.ApiConditionEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionDataDTO {

    public ApiConditionEnum apiConditionEnum;

    public String data;

}
