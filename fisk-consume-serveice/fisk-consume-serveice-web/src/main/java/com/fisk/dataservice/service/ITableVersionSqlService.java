package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.dataservice.dto.api.VersionSqlDTO;
import com.fisk.dataservice.entity.TableVersionSqlPO;

import java.util.List;

/**
 * @author 56263
 * @description 针对表【tb_version_sql】的数据库操作Service
 * @createDate 2023-12-27 09:47:32
 */
public interface ITableVersionSqlService extends IService<TableVersionSqlPO> {

    /**
     * 通过表id和表类型获取表的所有版本sql
     *
     * @param apiId 表id
     * @return
     */
    List<VersionSqlDTO> getVersionSqlByTableIdAndType(Integer apiId);

}
