package com.fisk.datamodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamodel.entity.DataSourceAreaPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: Lock
 */
@Mapper
public interface DataSourceAreaMapper extends BaseMapper<DataSourceAreaPO> {

}
