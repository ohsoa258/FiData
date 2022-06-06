package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.DatamaskingConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.entity.datasecurity.DatamaskingConfigPO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
public interface DatamaskingConfigService extends IService<DatamaskingConfigPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    DatamaskingConfigDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(DatamaskingConfigDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(DatamaskingConfigDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 获取数据脱敏列表
     *
     * @return list
     */
    List<DatamaskingConfigDTO> getList(DataSourceIdDTO dto);
}

