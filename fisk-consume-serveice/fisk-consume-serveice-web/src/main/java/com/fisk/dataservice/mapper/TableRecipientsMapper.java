package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.TableRecipientsPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TableRecipientsMapper extends FKBaseMapper<TableRecipientsPO> {
}
