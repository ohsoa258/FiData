package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.vo.AppDataSourceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: Lock
 * @data: 2021/5/26 16:09
 */
@Mapper
public interface AppDataSourceMapper extends BaseMapper<AppDataSourcePO> {

}
