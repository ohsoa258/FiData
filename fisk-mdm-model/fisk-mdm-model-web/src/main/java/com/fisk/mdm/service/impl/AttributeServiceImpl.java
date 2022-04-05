package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.service.AttributeService;
import org.springframework.stereotype.Service;

/**
 * @author WangYan
 * @date 2022/4/5 14:49
 */
@Service
public class AttributeServiceImpl extends ServiceImpl<AttributeMapper, AttributePO> implements AttributeService {
}
