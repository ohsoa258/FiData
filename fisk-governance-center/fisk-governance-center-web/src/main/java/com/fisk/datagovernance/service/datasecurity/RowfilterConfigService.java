package com.fisk.datagovernance.service.datasecurity;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowfilterConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowfilterConfigPO;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-30 15:29:16
 */
public interface RowfilterConfigService extends IService<RowfilterConfigPO> {

    /**
     * 回显: 根据id查询数据
     *
     * @param id id
     * @return 查询结果
     */
    RowfilterConfigDTO getData(long id);

    /**
     * 添加
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(RowfilterConfigDTO dto);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(RowfilterConfigDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);
}

