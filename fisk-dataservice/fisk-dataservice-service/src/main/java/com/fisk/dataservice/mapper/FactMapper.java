package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataservice.entity.FactPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangYan
 * @date 2021/8/13 11:48
 */
@Mapper
public interface FactMapper extends BaseMapper<FactPO> {

}
