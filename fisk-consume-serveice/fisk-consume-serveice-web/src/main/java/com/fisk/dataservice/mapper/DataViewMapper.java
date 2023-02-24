package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.DataViewPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper
public interface DataViewMapper extends FKBaseMapper<DataViewPO> {
}
