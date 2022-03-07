package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日志 mapper
 *
 * @author dick
 */
@Mapper
public interface LogsMapper extends FKBaseMapper<LogPO> {

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param apiIds apiIds
     * @param appId appId
     * @return 查询结果
     */
    Page<ApiLogVO> filter(Page<ApiLogVO> page, @Param("apiIds") List<Integer> apiIds, @Param("appId") Integer appId);
}
