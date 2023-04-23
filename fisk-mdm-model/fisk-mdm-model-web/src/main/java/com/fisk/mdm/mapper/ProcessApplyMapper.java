package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.dto.process.AllApprovalDTO;
import com.fisk.mdm.dto.process.EndingApprovalDTO;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.dto.process.ProcessApplyDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.vo.process.AllApprovalVO;
import com.fisk.mdm.vo.process.EndingApprovalVO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
@Mapper
public interface ProcessApplyMapper extends BaseMapper<ProcessApplyPO> {

    Page<ProcessApplyVO> getMyProcessApply(Page<ProcessApplyVO> page,@Param("userId")long userId,@Param("query") ProcessApplyDTO dto);
    Page<PendingApprovalVO> getPendingApproval(Page<PendingApprovalVO> page,
                                               @Param("userId")long userId,
                                               @Param("queryUserId")List<Integer> queryUserId,
                                               @Param("roleIds") List<Long> roleIds,
                                               @Param("query")PendingApprovalDTO dto);

    Page<AllApprovalVO> getAllApproval(Page<AllApprovalVO> page,
                                       @Param("userId")long userId,
                                       @Param("queryUserId")List<Integer> queryUserId,
                                       @Param("roleIds") List<Long> roleIds,
                                       @Param("query")AllApprovalDTO dto);
    Page<EndingApprovalVO> getOverApproval(Page<EndingApprovalVO> page,
                                           @Param("userId")long userId,
                                           @Param("queryUserId")List<Integer> queryUserId,
                                           @Param("query") EndingApprovalDTO dto);
}
