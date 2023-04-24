package com.fisk.mdm.dto.access;

import com.fisk.mdm.dto.access.CustomScriptDTO;
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
