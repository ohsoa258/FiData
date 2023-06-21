package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.TableRecipientsPO;
import com.fisk.dataservice.mapper.TableRecipientsMapper;
import com.fisk.dataservice.service.ITableRecipientsManageService;
import org.springframework.stereotype.Service;

@Service
public class TableRecipientsManageImpl
        extends ServiceImpl<TableRecipientsMapper, TableRecipientsPO>
        implements ITableRecipientsManageService {

}
