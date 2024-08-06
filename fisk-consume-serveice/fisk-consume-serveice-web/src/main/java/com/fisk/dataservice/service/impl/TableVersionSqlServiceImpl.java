package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.dto.api.VersionSqlDTO;
import com.fisk.dataservice.entity.TableVersionSqlPO;
import com.fisk.dataservice.map.VersionSqlMap;
import com.fisk.dataservice.mapper.TableVersionSqlPOMapper;
import com.fisk.dataservice.service.ITableVersionSqlService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_version_sql】的数据库操作Service实现
 * @createDate 2023-12-27 09:47:32
 */
@Service
public class TableVersionSqlServiceImpl extends ServiceImpl<TableVersionSqlPOMapper, TableVersionSqlPO>
        implements ITableVersionSqlService {

    /**
     * 通过表id和表类型获取表的所有版本sql
     *
     * @param apiId 表id
     * @return
     */
    @Override
    public List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer apiId) {
        LambdaQueryWrapper<TableVersionSqlPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableVersionSqlPO::getApiId, apiId);
        List<TableVersionSqlPO> list = list(wrapper);
        return VersionSqlMap.INSTANCES.poListToDtoList(list);
    }
  
}




