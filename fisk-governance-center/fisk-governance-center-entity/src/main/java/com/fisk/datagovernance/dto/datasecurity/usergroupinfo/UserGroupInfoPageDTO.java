package com.fisk.datagovernance.dto.datasecurity.usergroupinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class UserGroupInfoPageDTO extends UserGroupInfoDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

}
