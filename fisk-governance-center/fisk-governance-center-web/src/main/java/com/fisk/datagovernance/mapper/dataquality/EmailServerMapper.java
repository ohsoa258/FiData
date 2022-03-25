package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.EmailServerPO;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    Page<EmailServerVO> getAll(Page<EmailServerVO> page,  @Param("keyword") String keyword);
}