package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.app.AppApiSubQueryDTO;
import com.fisk.dataservice.entity.AppApiPO;
import com.fisk.dataservice.entity.ParmConfigPO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * api参数 mapper
 *
 * @author dick
 */
@Mapper
public interface ApiParmMapper extends FKBaseMapper<ParmConfigPO>
{

}
