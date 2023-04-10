package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
@Mapper
public interface ProcessApplyMapper extends BaseMapper<ProcessApplyPO> {
    Page<PendingApprovalVO> getPendingApproval(Page<PendingApprovalVO> page,@Param("userId")long userId,@Param("query")PendingApprovalDTO dto);

    Page<PendingApprovalVO> getOverApproval(Page<PendingApprovalVO> page,@Param("userId")long userId,@Param("query")PendingApprovalDTO dto);
}
