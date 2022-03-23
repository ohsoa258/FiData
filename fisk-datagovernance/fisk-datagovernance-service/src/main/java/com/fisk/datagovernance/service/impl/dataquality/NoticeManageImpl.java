package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.NoticePO;
import com.fisk.datagovernance.mapper.dataquality.NoticeMapper;
import com.fisk.datagovernance.service.dataquality.INoticeManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知实现类
 * @date 2022/3/23 12:56
 */
@Service
public class NoticeManageImpl extends ServiceImpl<NoticeMapper, NoticePO> implements INoticeManageService {

}