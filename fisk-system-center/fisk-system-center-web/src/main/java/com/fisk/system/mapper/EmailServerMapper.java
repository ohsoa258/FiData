package com.fisk.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.entity.EmailServerPO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器配置
 * @date 2022/3/23 12:42
 */
@Mapper
public interface EmailServerMapper extends FKBaseMapper<EmailServerPO> {
    /**
     * 查询邮件服务器分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @return 查询结果
     */
    Page<EmailServerVO> getPageAll(Page<EmailServerVO> page, @Param("keyword") String keyword);

    List<EmailServerVO> getAll();

    EmailServerVO getById(@Param("id") int id);

    EmailServerVO getDefaultEmailServer();
}