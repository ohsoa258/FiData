package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppApiSubDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterQueryDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerAppConfigPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import com.fisk.dataservice.dto.app.AppApiSubSaveDTO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-10-14 14:17:55
 */
public interface DatacheckServerAppConfigService extends IService<DatacheckServerAppConfigPO> {

    /**
     * 筛选器
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<AppRegisterVO> pageFilter(AppRegisterQueryDTO query);

    /**
     * 添加数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(AppRegisterDTO dto);

    /**
     * 编辑数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(AppRegisterEditDTO dto);

    /**
     * 删除数据
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 获取过滤器表字段
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * app订阅api
     */
    ResultEnum appSubscribe(AppApiSubDTO dto);
}

