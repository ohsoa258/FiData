package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 附件Mapper
 * @date 2022/8/15 9:58
 */
@Mapper
public interface AttachmentInfoMapper extends FKBaseMapper<AttachmentInfoPO> {

}
