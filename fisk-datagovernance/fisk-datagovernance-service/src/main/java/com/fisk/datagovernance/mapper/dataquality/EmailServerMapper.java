package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.EmailServerPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置
 * @date 2022/3/23 12:42
 */
@Mapper
public interface EmailServerMapper extends FKBaseMapper<EmailServerPO> {
}