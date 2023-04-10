package com.fisk.mdm.vo.process;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 * @Description:
 */
@Data
public class ProcessApplyVO {
    private Integer id;
    private String description;
    private String state;
    private String operationType;
    private LocalDateTime applicationTime;

}
