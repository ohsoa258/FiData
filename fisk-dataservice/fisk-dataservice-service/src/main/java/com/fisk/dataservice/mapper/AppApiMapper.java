package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.app.AppApiSubQueryDTO;
import com.fisk.dataservice.entity.AppApiPO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 应用api mapper
 *
 * @author dick
 */
@Mapper
public interface AppApiMapper extends FKBaseMapper<AppApiPO>
{
    Page<AppApiSubVO> getSubscribeAll(Page<AppApiSubVO> page, @Param("query") AppApiSubQueryDTO query);
}
