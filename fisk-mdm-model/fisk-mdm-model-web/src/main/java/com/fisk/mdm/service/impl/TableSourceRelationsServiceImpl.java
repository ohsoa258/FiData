package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.TableSourceRelationsPO;
import com.fisk.mdm.mapper.TableSourceRelationsMapper;
import com.fisk.mdm.service.TableSourceRelationsService;
import org.springframework.stereotype.Service;

@Service("tableSourceRelationsService")
public class TableSourceRelationsServiceImpl extends ServiceImpl<TableSourceRelationsMapper, TableSourceRelationsPO> implements TableSourceRelationsService {


}
