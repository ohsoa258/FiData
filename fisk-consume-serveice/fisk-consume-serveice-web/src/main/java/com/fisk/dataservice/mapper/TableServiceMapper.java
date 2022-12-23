package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.entity.TableServicePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface TableServiceMapper extends FKBaseMapper<TableServicePO> {

    /**
     * 分页查询表服务数据
     *
     * @param page
     * @param query
     * @return
     */
    Page<TableServicePageDataDTO> getTableServiceListData(Page<TableServicePageDataDTO> page, @Param("query") TableServicePageQueryDTO query);

}
