package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.api.ApiRegisterQueryDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.ApiRegisterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * api注册mapper
 *
 * @author dick
 */
@Mapper
public interface ApiRegisterMapper extends FKBaseMapper<ApiConfigPO> {
    /**
     * api列表分页功能
     *
     * @param page  分页对象
     * @return 查询结果
     */
    Page<ApiRegisterVO> getAll(Page<ApiRegisterVO> page, @Param("query") ApiRegisterQueryDTO query);
}
