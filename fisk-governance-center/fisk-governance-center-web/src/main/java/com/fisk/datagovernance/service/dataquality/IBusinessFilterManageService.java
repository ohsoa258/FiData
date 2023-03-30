package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessTaskDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗接口
 * @date 2022/3/23 12:22
 */
public interface IBusinessFilterManageService extends IService<BusinessFilterPO> {
    /**
     * 分页查询
     *
     * @return 分页列表
     */
    List<BusinessFilterVO> getAllRule(BusinessFilterQueryDTO query);

    /**
     * 添加数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(BusinessFilterDTO dto);

    /**
     * 编辑数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(BusinessFilterEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(int id);

    /**
     * 修改清洗规则执行顺序
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editFilterRuleSort(List<BusinessFilterSortDto> dto);

    /**
     * 查询工作区组件
     *
     * @return 执行结果
     */
    List<BusinessFilter_ProcessAssemblyVO> getProcessAssembly();

    /**
     * 查询工作区流程
     *
     * @param ruleId
     * @return 执行结果
     */
    ResultEntity<List<BusinessFilter_ProcessTaskVO>> getProcessDetail(long ruleId);

    /**
     * 新增工作区流程
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addProcess(BusinessFilter_ProcessTaskDTO dto);

    /**
     * 修改工作区流程
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editProcess(BusinessFilter_ProcessTaskDTO dto);

    /**
     * 调用工作区流程
     *
     * @param ruleId
     * @return 执行结果
     */
    ResultEntity<List<BusinessFilterResultVO>> collProcess(long ruleId);

    /**
     * API清洗,调用授权API获取Token
     *
     * @return 执行结果
     */
    ResultEntity<String> collAuthApi(BusinessFilterSaveDTO dto);

    /**
     * API清洗，调用API清洗数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum collApi(BusinessFilterSaveDTO dto);
}
