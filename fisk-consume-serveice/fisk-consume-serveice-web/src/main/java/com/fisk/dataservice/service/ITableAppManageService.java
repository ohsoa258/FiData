package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.dataservice.dto.tableservice.TableAppDTO;
import com.fisk.dataservice.dto.tableservice.TableAppDatasourceDTO;
import com.fisk.dataservice.dto.tableservice.TableAppQueryDTO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;

import java.util.List;

public interface ITableAppManageService extends IService<TableAppPO> {
    /**
     * 获取过滤器表字段
     *
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 筛选器
     *
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<TableAppVO> pageFilter(TableAppQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(TableAppDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(TableAppDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 检查数据源是否没有被引用
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum checkDataSourceIsNoUse(TableAppDatasourceDTO dto);
}
