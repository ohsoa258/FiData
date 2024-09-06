package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckRuleGroupDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckStandardsGroupQueryDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DatacheckStandardsGroupDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckStandardsGroupPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckRuleGroupVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DatacheckStandardsGroupVO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;

import java.util.List;


/**
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-04-23 15:35:47
 */
public interface IDatacheckStandardsGroupService extends IService<DatacheckStandardsGroupPO> {

    /**
     * 获取数据校验数据元标准组中数据
     * @param dto
     * @return
     */
    PageDTO<DatacheckStandardsGroupVO> getDataCheckStandardsGroup(DataCheckStandardsGroupQueryDTO dto);

    /**
     * 添加数据校验数据元标准组
     *
     * @param dto
     * @return
     */
    ResultEnum addDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto);

    /**
     * 修改数据校验数据元标准组
     *
     * @param dto
     * @return
     */
    ResultEnum editDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto);

    /**
     * 删除数据校验数据元标准组
     *
     * @param id
     * @return
     */
    ResultEnum deleteDataCheckStandardsGroup(Integer id);


    /**
     * 删除数据校验数据元标准组
     *
     * @param menuId
     * @return
     */
    ResultEnum deleteDataCheckStandardsGroupByMenuId(Integer menuId);

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckRuleGroupVO>
     * @description 根据数据元目录ID获取数据元校验规则组
     * @author dick
     * @date 2024/8/23 10:10
     * @version v1.0
     * @params dto
     */
    List<DataCheckRuleGroupVO> getRuleGroupByStandardMenuIds(DataCheckRuleGroupDTO dto);

    ResultEnum editDataCheckByStandards(StandardsDTO standardsDTO);
}

