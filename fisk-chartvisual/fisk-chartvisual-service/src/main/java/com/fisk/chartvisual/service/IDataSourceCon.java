package com.fisk.chartvisual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.response.ResultEnum;

import java.util.List;

/**
 * 数据源管理
 *
 * @author gy
 */
public interface IDataSourceCon extends IService<DataSourceConPO> {

    /**
     * 获取权限内所有的数据源
     * @return 查询结果
     */
    List<DataSourceConVO> listDataSourceCons();

    /**
     * 保存数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum saveDataSourceCon(DataSourceConDTO dto);

    /**
     * 更新数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateDataSourceCon(DataSourceConEditDTO dto);

    /**
     * 删除数据
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteDataSourceCon(int id);
}
