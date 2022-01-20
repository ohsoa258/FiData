package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.entity.TableBusinessPO;
import com.fisk.datamodel.mapper.TableBusinessMapper;
import com.fisk.datamodel.service.ITableBusiness;
import org.springframework.stereotype.Service;

/**
 * @author JianWenYang
 */
@Service
public class TableBusinessImpl
        extends ServiceImpl<TableBusinessMapper, TableBusinessPO>
        implements ITableBusiness {
}
