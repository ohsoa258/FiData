package com.fisk.auth.dto.ssologin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author lsj
 */
@Data
public class TicketInfoDTO {

    /**
     * 票据id
     */
    @JSONField(name = "TICKETID")
    private String TICKETID;

}
