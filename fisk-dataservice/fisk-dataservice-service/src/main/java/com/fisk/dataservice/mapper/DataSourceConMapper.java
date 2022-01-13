package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.datasource.DataSourceConQuery;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.common.mybatis.FKBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据源连接mapper
 *
 * @author dick
 */
@Mapper
public interface DataSourceConMapper extends FKBaseMapper<DataSourceConPO> {

    /**
     * 获取权限下所有数据源连接
     * @param page 分页信息
     * @param query where条件
     * @return 查询结果
     */
    Page<DataSourceConVO> listDataSourceConByUserId(Page<DataSourceConVO> page, @Param("query") DataSourceConQuery query);

    /**
     * 根据id查询数据
     * @param id id
     * @return 数据对象
     */
    DataSourceConVO getDataSourceConByUserId(int id);
}
