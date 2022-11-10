package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import com.fisk.datagovernance.mapper.dataquality.AttachmentInfoMapper;
import com.fisk.datagovernance.service.dataquality.IAttachmentInfoManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 附件接口实现类
 * @date 2022/8/15 9:56
 */
@Service
public class AttachmentInfoImpl  extends ServiceImpl<AttachmentInfoMapper, AttachmentInfoPO> implements IAttachmentInfoManageService {

}
