package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.NoticePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知
 * @date 2022/3/23 12:42
 */
@Mapper
public interface NoticeMapper extends FKBaseMapper<NoticePO> {
}