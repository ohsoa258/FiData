package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_SaveProcessDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.filterresult.BusinessFilterResultVO;
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
     * 保存工作区流程
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum saveProcess(BusinessFilter_SaveProcessDTO dto);

    /**
     * 调用工作区流程
     *
     * @param ruleId
     * @return 执行结果
     */
    ResultEntity<List<BusinessFilterResultVO>> collProcess(long ruleId);

    /**
     * 获取工作区的task的code
     *
     * @return 执行结果
     */
    String getProcessTaskCode();

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

    /**
     * 解析cron表达式执行频率
     *
     * @param dto dto
     * @return 执行结果
     */
    List<String> getNextCronExeTime(NextCronTimeDTO dto);
}
