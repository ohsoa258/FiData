package com.fisk.chartvisual.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.chartvisual.dto.DataSourceConDTO;
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
     * @return
     */
    ResultEnum saveDataSourceCon(DataSourceConDTO dto);
}
