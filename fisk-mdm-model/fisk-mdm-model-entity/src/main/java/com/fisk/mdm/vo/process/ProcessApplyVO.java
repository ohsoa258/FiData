package com.fisk.mdm.vo.process;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.mdm.enums.ApprovalApplyStateEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 */
@Data
public class ProcessApplyVO {
    private Integer id;
    private String description;
    private ApprovalApplyStateEnum state;
    private EventTypeEnum operationType;
    @JsonFormat(shape =JsonFormat.Shape.STRING,pattern ="yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private LocalDateTime applicationTime;

}
