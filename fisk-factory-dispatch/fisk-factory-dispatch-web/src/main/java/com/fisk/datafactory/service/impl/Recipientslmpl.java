package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.entity.RecipientsPO;
import com.fisk.datafactory.mapper.RecipientsMapper;
import com.fisk.datafactory.service.IRecipients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Recipientslmpl extends ServiceImpl<RecipientsMapper, RecipientsPO> implements IRecipients {
}
