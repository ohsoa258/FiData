package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗，API清洗配置
 * @date 2022/10/8 15:34
 */
@Data
@TableName("tb_bizfilter_api_config")
public class BusinessFilterApiConfigPO extends BasePO {

    /**
     * tb_bizfilter_rule表主键ID
     */
    public String ruleId;

    /**
     * api授权地址
     */
    public String apiAuthAddress;

    /**
     * api授权body类型
     */
    public String apiAuthBodyType;

    /**
     * api授权有效时间，页面自行判断，当前时间<授权有效时间
     */
    public LocalDateTime apiAuthExpirTime;

    /**
     * api授权有效时间，分钟
     */
    public int apiAuthExpirMinute;

    /**
     * api授权票据
     */
    public String apiAuthTicket;

    /**
     * api授权票据状态 1：有效 2：无效
     */
    // public int apiAuthTicketState;

    /**
     * api地址
     */
    public String apiAddress;

    /**
     * api body类型
     */
    public String apiBodyType;
}
