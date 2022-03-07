package com.fisk.auth.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.auth.dto.clientregister.ClientRegisterPageDTO;
import com.fisk.auth.entity.ClientRegisterPO;
import com.fisk.auth.vo.ClientRegisterVO;
import com.fisk.common.mybatis.FKBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
@Mapper
public interface ClientRegisterMapper extends FKBaseMapper<ClientRegisterPO> {

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<ClientRegisterVO> filter(Page<ClientRegisterVO> page, @Param("query") ClientRegisterPageDTO query);
}
