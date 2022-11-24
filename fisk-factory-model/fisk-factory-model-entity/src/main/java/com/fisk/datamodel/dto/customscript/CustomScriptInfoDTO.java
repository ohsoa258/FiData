package com.fisk.datamodel.dto.customscript;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class CustomScriptInfoDTO extends CustomScriptDTO {

    public LocalDateTime createTime;

    public String createUser;

}
