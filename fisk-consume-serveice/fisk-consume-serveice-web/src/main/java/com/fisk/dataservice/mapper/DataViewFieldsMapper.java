package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.ViewFieldsPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Mapper
public interface DataViewFieldsMapper extends FKBaseMapper<ViewFieldsPO> {

}
