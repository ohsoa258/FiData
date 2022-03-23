package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.EmailServerPO;
import com.fisk.datagovernance.mapper.dataquality.EmailServerMapper;
import com.fisk.datagovernance.service.dataquality.IEmailServerManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 邮件配置实现类
 * @date 2022/3/23 12:56
 */
@Service
public class EmailServerManageImpl extends ServiceImpl<EmailServerMapper, EmailServerPO> implements IEmailServerManageService {

}