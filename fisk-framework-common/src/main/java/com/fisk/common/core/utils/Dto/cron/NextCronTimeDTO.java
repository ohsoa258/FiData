package com.fisk.common.core.utils.Dto.cron;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class NextCronTimeDTO {

    public String cronExpression;

    /**
     * 显示下次执行时间
     */
    public Integer number;

}
