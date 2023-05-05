package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.entity.ProcessInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Mapper
public interface ProcessInfoMapper extends BaseMapper<ProcessInfoPO> {
}
