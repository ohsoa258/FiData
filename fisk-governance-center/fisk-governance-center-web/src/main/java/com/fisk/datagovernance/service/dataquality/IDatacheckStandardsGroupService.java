package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DatacheckStandardsGroupDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckStandardsGroupPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DatacheckStandardsGroupVO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-04-23 15:35:47
 */
public interface IDatacheckStandardsGroupService extends IService<DatacheckStandardsGroupPO> {

    /**
     * 获取数据校验数据元标准组中数据
     *
     * @param standardsId
     * @return
     */
    List<DatacheckStandardsGroupVO> getDataCheckStandardsGroup(Integer standardsId);

    /**
     * 添加数据校验数据元标准组
     * @param dto
     * @return
     */
    ResultEnum addDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto);

    /**
     * 修改数据校验数据元标准组
     * @param dto
     * @return
     */
    ResultEnum editDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto);

    /**
     * 删除数据校验数据元标准组
     * @param id
     * @return
     */
    ResultEnum deleteDataCheckStandardsGroup(Integer id);
}

