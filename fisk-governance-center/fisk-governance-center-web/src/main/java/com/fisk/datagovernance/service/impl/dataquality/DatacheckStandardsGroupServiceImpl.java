package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.entity.dataquality.DatacheckStandardsGroupPO;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckExtendMap;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.map.dataquality.DatacheckStandardsGroupMap;
import com.fisk.datagovernance.mapper.dataquality.DataCheckExtendMapper;
import com.fisk.datagovernance.mapper.dataquality.DatacheckStandardsGroupMapper;
import com.fisk.datagovernance.mapper.dataquality.QualityReportMapper;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.service.dataquality.IDatacheckStandardsGroupService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckRuleGroupVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DatacheckStandardsGroupVO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportRuleVO;
import com.fisk.datagovernance.vo.dataquality.template.TemplateVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.enums.ValueRangeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service("datacheckStandardsGroupService")
@Slf4j
public class DatacheckStandardsGroupServiceImpl extends ServiceImpl<DatacheckStandardsGroupMapper, DatacheckStandardsGroupPO> implements IDatacheckStandardsGroupService {

    @Resource
    DataManageClient dataManageClient;

    @Resource
    IDataCheckManageService dataCheckManageService;

    @Resource
    DataCheckManageImpl dataCheckManageImpl;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private QualityReportMapper qualityReportMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private TemplateManageImpl templateManage;


    /**
     * 获取数据检查标准分组
     *
     * @param dto dto 根据此查询条件获取相应的标准分组信息
     * @return 返回一个包含数据检查标准分组信息的列表
     */
    @Override
    public PageDTO<DatacheckStandardsGroupVO> getDataCheckStandardsGroup(DataCheckStandardsGroupQueryDTO dto) {
        PageDTO<DatacheckStandardsGroupVO> pageDTO = new PageDTO<>();
        List<DatacheckStandardsGroupVO> groupDtoList = new ArrayList<>();
        int current = dto.getCurrent();
        int size = dto.getSize();
        // 根据菜单ID获取标准ID列表
        List<Integer> standardByMenuId = dataManageClient.getStandardByMenuId(dto.getStandardsMenuId());
        if (!CollectionUtils.isEmpty(standardByMenuId)) {
            // 查询条件构造，查询与标准ID列表匹配的所有标准分组信息
            LambdaQueryWrapper<DatacheckStandardsGroupPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DatacheckStandardsGroupPO::getStandardsId, standardByMenuId);
            if (dto.getGroupName()!=null){
                queryWrapper.like(DatacheckStandardsGroupPO::getCheckGroupName, dto.getGroupName());
            }
            if (dto.getTemplateId()!=null && dto.getTemplateId()!=0){
                queryWrapper.eq(DatacheckStandardsGroupPO::getTemplateId, dto.getTemplateId());
            }
            if (dto.getRuleExecuteNode()!=null && dto.getRuleExecuteNode()!=0){
                queryWrapper.eq(DatacheckStandardsGroupPO::getRuleExecuteNode, dto.getRuleExecuteNode());
            }
            queryWrapper.orderByDesc(DatacheckStandardsGroupPO::getCreateTime);
            List<DatacheckStandardsGroupPO> groupPOList = this.list(queryWrapper);
            // 如果查询结果为空，直接返回空列表
            if (CollectionUtils.isEmpty(groupPOList)) {
                return pageDTO;
            }
            // 将PO对象列表转换为VO对象列表
            current = current - 1;
            pageDTO.setTotal(Long.valueOf(groupPOList.size()));
            pageDTO.setTotalPage((long) Math.ceil(1.0 * groupPOList.size() / size));
            groupDtoList = groupPOList.stream().map(DatacheckStandardsGroupMap.INSTANCES::poToVo)
                    .skip((current - 1 + 1) * size).limit(size).collect(Collectors.toList());


            // 根据分组ID列表获取所有规则信息
            List<Integer> groupIds = groupPOList.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            List<DataCheckVO> allRules = dataCheckManageService.getRuleByIds(groupIds);

            // 如果规则信息为空，直接返回已构造的标准分组VO列表
            if (CollectionUtils.isEmpty(allRules)) {
                pageDTO.setItems(groupDtoList);
                return pageDTO;
            }
            // 获取所有规则ID的列表，去重后查询扩展信息
            List<Integer> ruleIds = allRules.stream().map(DataCheckVO::getId).distinct().collect(Collectors.toList());
            List<DataCheckExtendVO> dataCheckExtendVOList = dataCheckExtendMapper.getDataCheckExtendByRuleIdList(ruleIds);
            // 查询规则被报告的引用情况
            List<QualityReportRuleVO> qualityReportRuleVOList = qualityReportMapper.getByRuleIds(ruleIds);

            // 如果存在扩展信息，则为每条规则设置扩展信息
            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(dataCheckExtendVOList)) {
                allRules.forEach(t -> {
                    DataCheckExtendVO dataCheckExtendVO = dataCheckExtendVOList.stream().filter(k -> k.getRuleId() == t.getId()).findFirst().orElse(null);
                    t.setDataCheckExtend(dataCheckExtendVO);

                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(qualityReportRuleVOList)) {
                        List<String> reportNameList = qualityReportRuleVOList.stream().filter(k -> k.getRuleId() == t.getId()).map(QualityReportRuleVO::getReportName).collect(Collectors.toList());
                        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(reportNameList)) {
                            t.setBelongToReportNameList(reportNameList);
                        }
                    }
                });
            }
            // 根据指定的排序规则对规则信息进行排序
            allRules = allRules.stream().sorted(
                    // 1.先按照表名称排正序，并处理tableAlias为空的情况
                    Comparator.comparing(DataCheckVO::getTableAlias, Comparator.nullsFirst(Comparator.naturalOrder()))
                            // 2.再按照执行节点排正序，并处理ruleExecuteNode为空的情况
                            .thenComparing(DataCheckVO::getRuleExecuteNode, Comparator.nullsFirst(Comparator.naturalOrder()))
                            // 3.再按照创建时间排倒叙，并处理创建时间为空的情况
                            .thenComparing(DataCheckVO::getCreateTime, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
            // 根据数据检查组ID将规则信息分组，然后为每个标准分组VO设置规则信息列表
            if (!CollectionUtils.isEmpty(allRules)) {
                Map<Integer, List<DataCheckVO>> datacheckMap = allRules.stream()
                        .collect(groupingBy(DataCheckVO::getDatacheckGroupId));
                groupDtoList = groupDtoList.stream().map(i -> {
                    List<DataCheckVO> dataCheckDTOS = datacheckMap.get(i.getId());
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(dataCheckDTOS)) {
                        List<List<String>> belongToReportNameList = dataCheckDTOS.stream().filter(s -> !CollectionUtils.isEmpty(s.getBelongToReportNameList())).map(DataCheckVO::getBelongToReportNameList).collect(Collectors.toList());
                        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(belongToReportNameList)) {
                            List<String> dist_BelongToReportNameList = new ArrayList<>();
                            for (List<String> bReportNameList : belongToReportNameList) {
                                dist_BelongToReportNameList.addAll(bReportNameList);
                            }
                            dist_BelongToReportNameList = dist_BelongToReportNameList.stream().distinct().collect(Collectors.toList());
                            i.setBelongToReportNameList(dist_BelongToReportNameList);
                        }
                    }
                    i.setDataCheckList(dataCheckDTOS);
                    return i;
                }).collect(Collectors.toList());
            }
            pageDTO.setItems(groupDtoList);
        }
        return pageDTO;
    }

    /**
     * 添加数据校验数据元标准组
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum addDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto) {
        DatacheckStandardsGroupPO groupPO = DatacheckStandardsGroupMap.INSTANCES.dtoToPo(dto);
        LambdaQueryWrapper<DatacheckStandardsGroupPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatacheckStandardsGroupPO::getStandardsId, dto.getStandardsId());
        List<DatacheckStandardsGroupPO> group = this.list(queryWrapper);
        List<String> groupNames = group.stream().map(DatacheckStandardsGroupPO::getCheckGroupName).collect(Collectors.toList());
        if (groupNames.contains(dto.getCheckGroupName())) {
            throw new FkException(ResultEnum.CHECK_STANDARDS_GROUP_ERROR);
        }
        this.save(groupPO);
        List<DataCheckEditDTO> dataCheckList = dto.getDataCheckList();
        if (!CollectionUtils.isEmpty(dataCheckList)) {
            dataCheckList = dataCheckList.stream().map(i -> {
                Integer id = (int) groupPO.id;
                i.setDatacheckGroupId(id);
                String filedName = i.getDataCheckExtend().fieldName;
                i.ruleName = groupPO.getCheckGroupName() + i.tableName + filedName;
                return i;
            }).collect(Collectors.toList());
            ResultEnum ruleCheckResultEnum = dataCheckManageImpl.batchAddOrEditData(dataCheckList);
            if (ruleCheckResultEnum != ResultEnum.SUCCESS) {
                // 质量验证不通过，删除刚刚添加的数据元组
                this.removeById(groupPO.id);
                return ruleCheckResultEnum;
            }
            DataCheckEditDTO checkEditDTO = dataCheckList.get(0);
            DataCheckExtendDTO dataCheckExtend = checkEditDTO.getDataCheckExtend();
            groupPO.setRuleCheckType(checkEditDTO.getRuleCheckType());
            groupPO.setRuleExecuteNode(checkEditDTO.getRuleExecuteNode());
            groupPO.setTemplateId(checkEditDTO.getTemplateId());
            if (dataCheckExtend != null){
                groupPO.setRangeCheckType(dataCheckExtend.getRangeCheckType());
                groupPO.setRangeType(dataCheckExtend.getRangeType());
                groupPO.setRangeCheckValueRangeType(dataCheckExtend.getRangeCheckValueRangeType());
                groupPO.setRangeCheckKeywordIncludeType(dataCheckExtend.getRangeCheckKeywordIncludeType());
                groupPO.setRangeCheckOneWayOperator(dataCheckExtend.getRangeCheckOneWayOperator());
                groupPO.setRangeCheckValue(dataCheckExtend.getRangeCheckValue());
                groupPO.setStandardCheckType(dataCheckExtend.getStandardCheckType());
                groupPO.setStandardCheckCharRangeType(dataCheckExtend.getStandardCheckCharRangeType());
                groupPO.setStandardCheckTypeDateValue(dataCheckExtend.getStandardCheckTypeDateValue());
                groupPO.setStandardCheckTypeLengthSeparator(dataCheckExtend.getStandardCheckTypeLengthSeparator());
                groupPO.setStandardCheckTypeLengthOperator(dataCheckExtend.getStandardCheckTypeLengthOperator());
                groupPO.setStandardCheckTypeLengthValue(dataCheckExtend.getStandardCheckTypeLengthValue());
                groupPO.setStandardCheckTypeRegexpValue(dataCheckExtend.getStandardCheckTypeRegexpValue());
                groupPO.setFluctuateCheckType(dataCheckExtend.getFluctuateCheckType());
                groupPO.setFluctuateCheckOperator(dataCheckExtend.getFluctuateCheckOperator());
                groupPO.setFluctuateCheckValue(dataCheckExtend.getFluctuateCheckValue());
                groupPO.setParentageCheckType(dataCheckExtend.getParentageCheckType());
                groupPO.setRegexpCheckValue(dataCheckExtend.getRegexpCheckValue());
                groupPO.setRecordErrorData(dataCheckExtend.getRecordErrorData());
                groupPO.setErrorDataRetentionTime(dataCheckExtend.getErrorDataRetentionTime());
            }
            this.updateById(groupPO);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 编辑数据校验数据元标准组
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEnum editDataCheckStandardsGroup(DatacheckStandardsGroupDTO dto) {

        DatacheckStandardsGroupPO groupPO = DatacheckStandardsGroupMap.INSTANCES.dtoToPo(dto);
        LambdaQueryWrapper<DatacheckStandardsGroupPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatacheckStandardsGroupPO::getStandardsId, dto.getStandardsId())
                .ne(DatacheckStandardsGroupPO::getId, dto.getId());
        List<DatacheckStandardsGroupPO> group = this.list(queryWrapper);
        List<String> groupNames = group.stream().map(DatacheckStandardsGroupPO::getCheckGroupName).collect(Collectors.toList());
        List<String> names = groupNames.stream().filter(i -> i.equals(dto.getCheckGroupName())).collect(Collectors.toList());
        if (names.size() > 0) {
            throw new FkException(ResultEnum.CHECK_STANDARDS_GROUP_ERROR);
        }
        Integer templateId = 0;
        List<DataCheckEditDTO> dataCheckEditList = dto.getDataCheckList();
        if (!CollectionUtils.isEmpty(dataCheckEditList)) {
            templateId = dataCheckEditList.get(0).getTemplateId();
            dataCheckEditList = dataCheckEditList.stream().map(i -> {
                Integer id = (int) groupPO.id;
                i.setDatacheckGroupId(id);
                i.ruleName = groupPO.getCheckGroupName() + i.tableName + i.getDataCheckExtend().fieldName;
                return i;
            }).collect(Collectors.toList());

            // 查询数据库中是否存在不在当前组下面的规则，存在则删除
            List<Integer> dataCheckIds = dataCheckEditList.stream().filter(t -> t.getId() != 0).map(i -> i.getId()).collect(Collectors.toList());
            LambdaQueryWrapper<DataCheckPO> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DataCheckPO::getDatacheckGroupId, groupPO.id);
            queryWrapper1.eq(DataCheckPO::getTemplateId, templateId);
            List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper1);
            for (DataCheckPO dataCheckPO : dataCheckPOS) {
                if (!dataCheckIds.contains((int) dataCheckPO.id)) {
                    dataCheckManageService.deleteData((int) dataCheckPO.id);
                }
            }

            ResultEnum ruleCheckResultEnum = dataCheckManageImpl.batchAddOrEditData(dataCheckEditList);
            if (ruleCheckResultEnum != ResultEnum.SUCCESS) {
                return ruleCheckResultEnum;
            }
        } else {
            List<TemplateVO> templates = templateManage.getAll();
            List<TemplateVO> templateVOList = templates.stream().filter(i -> i.getTemplateType() == TemplateTypeEnum.RANGE_CHECK).collect(Collectors.toList());
            LambdaQueryWrapper<DataCheckPO> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DataCheckPO::getDatacheckGroupId, groupPO.id);
            queryWrapper1.eq(DataCheckPO::getTemplateId, templateVOList.get(0).id);
            List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper1);
            for (DataCheckPO dataCheckPO : dataCheckPOS) {
                dataCheckManageService.deleteData((int) dataCheckPO.id);
            }
        }
        // 质量规则验证保存通过再保存组信息
        this.updateById(groupPO);
        return ResultEnum.SUCCESS;
    }

    /**
     * 删除数据校验数据元标准组
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteDataCheckStandardsGroup(Integer id) {
        this.removeById(id);
        LambdaQueryWrapper<DataCheckPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataCheckPO::getDatacheckGroupId, id);
        List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(dataCheckPOS)) {
            dataCheckPOS.stream().forEach(dataCheckPO -> {
                dataCheckManageService.deleteData((int) dataCheckPO.id);
            });
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDataCheckStandardsGroupByMenuId(Integer menuId) {
        LambdaQueryWrapper<DatacheckStandardsGroupPO> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.in(DatacheckStandardsGroupPO::getStandardsMenuId, menuId);
        List<DatacheckStandardsGroupPO> groups = this.list(deleteWrapper);
        List<Integer> groupIds = groups.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        this.removeByIds(groupIds);
        LambdaQueryWrapper<DataCheckPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DataCheckPO::getDatacheckGroupId, groupIds);
        List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(dataCheckPOS)) {
            dataCheckPOS.stream().forEach(dataCheckPO -> {
                dataCheckManageService.deleteData((int) dataCheckPO.id);
            });
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<DataCheckRuleGroupVO> getRuleGroupByStandardMenuIds(DataCheckRuleGroupDTO dto) {
        List<DataCheckRuleGroupVO> ruleGroupVOS = new ArrayList<>();
        if (dto == null || CollectionUtils.isEmpty(dto.getStandardMenuIdList())) {
            return ruleGroupVOS;
        }
        QueryWrapper<DatacheckStandardsGroupPO> dataCheckStandardsGroupPOQueryWrapper = new QueryWrapper<>();
        dataCheckStandardsGroupPOQueryWrapper.lambda().eq(DatacheckStandardsGroupPO::getDelFlag, 1)
                .in(DatacheckStandardsGroupPO::getStandardsMenuId, dto.getStandardMenuIdList());
        List<DatacheckStandardsGroupPO> dataCheckStandardsGroupPOS = baseMapper.selectList(dataCheckStandardsGroupPOQueryWrapper);
        if (!CollectionUtils.isEmpty(dataCheckStandardsGroupPOS)) {
            dataCheckStandardsGroupPOS.forEach(t -> {
                DataCheckRuleGroupVO dataCheckRuleGroupVO = new DataCheckRuleGroupVO();
                dataCheckRuleGroupVO.setStandardsMenuId(t.getStandardsMenuId());
                dataCheckRuleGroupVO.setStandardsId(t.getStandardsId());
                dataCheckRuleGroupVO.setDataCheckGroupId(Math.toIntExact(t.getId()));
                dataCheckRuleGroupVO.setCheckGroupName(t.getCheckGroupName());
                ruleGroupVOS.add(dataCheckRuleGroupVO);
            });
        }
        return ruleGroupVOS;
    }

    @Override
    public ResultEnum editDataCheckByStandards(StandardsDTO standardsDTO) {
        List<TemplateVO> templates = templateManage.getAll();
        List<TemplateVO> templateVOList = templates.stream().filter(i -> i.getTemplateType() == TemplateTypeEnum.RANGE_CHECK).collect(Collectors.toList());
        LambdaQueryWrapper<DatacheckStandardsGroupPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DatacheckStandardsGroupPO::getStandardsId, standardsDTO.getId());
        List<DatacheckStandardsGroupPO> group = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(group)) {
            return ResultEnum.SUCCESS;
        }
        List<Long> groupIds = group.stream().map(BasePO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<DataCheckPO> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(DataCheckPO::getDatacheckGroupId, groupIds);
        queryWrapper1.eq(DataCheckPO::getTemplateId, templateVOList.get(0).id);
        List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper1);
        Map<Integer, List<DataCheckPO>> groupMap = dataCheckPOS.stream().collect(groupingBy(DataCheckPO::getDatacheckGroupId));
        List<StandardsBeCitedDTO> standardsBeCitedDTOList = standardsDTO.getStandardsBeCitedDTOList();
        List<DataCheckExtendPO> dataCheckExtendPOS = null;
        if (!CollectionUtils.isEmpty(dataCheckPOS)) {
            List<Long> ruleIdList = dataCheckPOS.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckExtendPO> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().in(DataCheckExtendPO::getRuleId, ruleIdList);
            dataCheckExtendPOS = dataCheckExtendMapper.selectList(queryWrapper2);
        }
        for (DatacheckStandardsGroupPO groupPO : group) {
            DatacheckStandardsGroupDTO dto = new DatacheckStandardsGroupDTO();
            List<DataCheckPO> dataCheckPOList = groupMap.get((int) groupPO.id);
            dto.setId((int) groupPO.id);
            dto.setStandardsId(standardsDTO.getId());
            dto.setStandardsMenuId(standardsDTO.getMenuId());
            dto.setStandardsId(standardsDTO.getId());
            dto.setCheckGroupName(groupPO.getCheckGroupName());
            dto.setChineseName(standardsDTO.getChineseName());
            dto.setEnglishName(standardsDTO.getEnglishName());
            dto.setDescription(standardsDTO.getDescription());
            dto.setFieldType(standardsDTO.getFieldType());
            dto.setDatametaCode(standardsDTO.getDatametaCode());

            List<DataCheckEditDTO> dataCheckEditList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(standardsBeCitedDTOList)) {
                List<CodeSetDTO> codeSetDTOList = standardsDTO.getCodeSetDTOList();
                for (StandardsBeCitedDTO standardsBeCitedDTO : standardsBeCitedDTOList) {
                    Integer dbId = standardsBeCitedDTO.getDbId();
                    DataCheckEditDTO checkEditDTO = new DataCheckEditDTO();
//                    DataCheckPO dataCheckPO1 = dataCheckPOList.get(0);
                    checkEditDTO.setDatacheckGroupId((int) groupPO.id);
                    checkEditDTO.setRuleCheckType(groupPO.getRuleCheckType());
                    checkEditDTO.setDatasourceId(standardsBeCitedDTO.getDbId());
                    checkEditDTO.setRuleDescribe(groupPO.getDescription());
                    checkEditDTO.setRuleExecuteNode(groupPO.getRuleExecuteNode());

//                    if (!CollectionUtils.isEmpty(dataCheckExtendPOS)) {
//                        DataCheckExtendPO dataCheckExtendPO = dataCheckExtendPOS.stream().filter(f -> f.getRuleId() == dataCheckPO1.getId()).findFirst().orElse(null);
//                        if (dataCheckExtendPO != null && StringUtils.isNotEmpty(dataCheckPO1.getRuleIllustrate())) {
//                            String ruleIllustrate = dataCheckPO1.getRuleIllustrate().replace(dataCheckExtendPO.getFieldName(), standardsBeCitedDTO.getFieldName());
//                            checkEditDTO.setRuleIllustrate(ruleIllustrate);
//                        }
//                    }


                    checkEditDTO.setTableBusinessType(standardsBeCitedDTO.getTableBusinessType().getValue());
                    checkEditDTO.setRuleName(groupPO.getCheckGroupName() + standardsBeCitedDTO.getTableName() + standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setRuleState(RuleStateEnum.Enable);
                    //checkEditDTO.setTableDescribe(standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setSchemaName(standardsBeCitedDTO.getSchemaName());
                    checkEditDTO.setTableUnique(standardsBeCitedDTO.getTableId());
                    checkEditDTO.setTableName(standardsBeCitedDTO.getTableName());
                    checkEditDTO.setFieldUnique(standardsBeCitedDTO.getFieldId());
                    checkEditDTO.setFieldName(standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setTableType(TableTypeEnum.TABLE);
                    checkEditDTO.setTemplateId(templateVOList.get(0).id);
                    checkEditDTO.setSourceType(SourceTypeEnum.FiData);
                    checkEditDTO.setTableDescribe(groupPO.getDescription());

                    if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.DATASET){
                        String ruleIllustrate = "检查"+checkEditDTO.getFieldName()+"字段值是否在指定的序列范围内，配置的序列范围为："+codeSetDTOList.stream().map(CodeSetDTO::getName).collect(Collectors.joining(","));
                        checkEditDTO.setRuleIllustrate(ruleIllustrate);
                    }else if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.VALUE){
                            String ruleIllustrate = "检查"+checkEditDTO.getFieldName()+"字段值是否在指定的数值范围内，配置的数值范围为："+groupPO.getRangeCheckOneWayOperator()+groupPO.getRangeCheckValue();
                            checkEditDTO.setRuleIllustrate(ruleIllustrate);
                    }else if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.VALUE_RANGE){
                        String ruleIllustrate = "检查"+checkEditDTO.getFieldName()+"字段值是否在指定的数值范围内，配置的数值范围为："+groupPO.getRangeCheckValue();
                        checkEditDTO.setRuleIllustrate(ruleIllustrate);
                    }

                    if (!CollectionUtils.isEmpty(dataCheckPOList)){
                        for (DataCheckPO dataCheckPO : dataCheckPOList) {
                            if (dbId.equals(dataCheckPO.getDatasourceId())
                                    && standardsBeCitedDTO.getTableId().equals(dataCheckPO.getTableUnique())) {
                                LambdaQueryWrapper<DataCheckExtendPO> query = new LambdaQueryWrapper<>();
                                query.eq(DataCheckExtendPO::getRuleId, dataCheckPO.getId());
                                DataCheckExtendPO dataCheckExtendPO = dataCheckExtendMapper.selectOne(query);
                                if (standardsBeCitedDTO.getFieldId().equals(dataCheckExtendPO.fieldUnique)) {
                                    checkEditDTO = DataCheckMap.INSTANCES.poToDto_Edit(dataCheckPO);
                                }
                            }
                        }
                    }
                    dataCheckEditList.add(checkEditDTO);
                }
                List<DataCheckExtendDTO> dataCheckExtendDTOS = DataCheckExtendMap.INSTANCES.poListToDtoList(dataCheckExtendPOS);
                if (CollectionUtils.isEmpty(dataCheckExtendDTOS)){
                    dataCheckExtendDTOS = new ArrayList<>();
                    DataCheckExtendDTO dataCheckExtendDTO = new DataCheckExtendDTO();
                    dataCheckExtendDTO.setRangeCheckType(groupPO.getRangeCheckType());
                    dataCheckExtendDTO.setRangeType(groupPO.getRangeType());
                    dataCheckExtendDTO.setRangeCheckValueRangeType(groupPO.getRangeCheckValueRangeType());
                    dataCheckExtendDTO.setRangeCheckOneWayOperator(groupPO.getRangeCheckOneWayOperator());
                    dataCheckExtendDTO.setRangeCheckValue(groupPO.getRangeCheckValue());
                    if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.DATASET){
                        dataCheckExtendDTO.setRangeCheckType(RangeCheckTypeEnum.SEQUENCE_RANGE);
                        dataCheckExtendDTO.setRangeType(1);
                        dataCheckExtendDTO.setRangeCheckValue(codeSetDTOList.stream().map(CodeSetDTO::getName).collect(Collectors.joining(",")));
                    }else if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.VALUE){
                        dataCheckExtendDTO.setRangeCheckType(RangeCheckTypeEnum.VALUE_RANGE);
                        dataCheckExtendDTO.setRangeCheckValueRangeType(RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE);
                        dataCheckExtendDTO.setRangeCheckOneWayOperator(standardsDTO.getSymbols());
                        dataCheckExtendDTO.setRangeCheckValue(standardsDTO.getValueRange());
                        dataCheckExtendDTO.setRangeType(1);
                    }else if (standardsDTO.getValueRangeType() == ValueRangeTypeEnum.VALUE_RANGE){
                        dataCheckExtendDTO.setRangeCheckType(RangeCheckTypeEnum.VALUE_RANGE);
                        dataCheckExtendDTO.setRangeCheckValueRangeType(RangeCheckValueRangeTypeEnum.INTERVAL_VALUE);
                        dataCheckExtendDTO.setRangeType(1);
                        dataCheckExtendDTO.setRangeCheckOneWayOperator(standardsDTO.getSymbols());
                        dataCheckExtendDTO.setRangeCheckValue(standardsDTO.getValueRange());
                    }


                    dataCheckExtendDTO.setRangeCheckKeywordIncludeType(groupPO.getRangeCheckKeywordIncludeType());


                    dataCheckExtendDTO.setStandardCheckType(groupPO.getStandardCheckType());
                    dataCheckExtendDTO.setStandardCheckCharRangeType(groupPO.getStandardCheckCharRangeType());
                    dataCheckExtendDTO.setStandardCheckTypeDateValue(groupPO.getStandardCheckTypeDateValue());
                    dataCheckExtendDTO.setStandardCheckTypeLengthSeparator(groupPO.getStandardCheckTypeLengthSeparator());
                    dataCheckExtendDTO.setStandardCheckTypeLengthOperator(groupPO.getStandardCheckTypeLengthOperator());
                    dataCheckExtendDTO.setStandardCheckTypeLengthValue(groupPO.getStandardCheckTypeLengthValue());
                    dataCheckExtendDTO.setStandardCheckTypeRegexpValue(groupPO.getStandardCheckTypeRegexpValue());
                    dataCheckExtendDTO.setFluctuateCheckType(groupPO.getFluctuateCheckType());
                    dataCheckExtendDTO.setFluctuateCheckOperator(groupPO.getFluctuateCheckOperator());
                    dataCheckExtendDTO.setFluctuateCheckValue(groupPO.getFluctuateCheckValue());
                    dataCheckExtendDTO.setParentageCheckType(groupPO.getParentageCheckType());
                    dataCheckExtendDTO.setRegexpCheckValue(groupPO.getRegexpCheckValue());
                    dataCheckExtendDTO.setRecordErrorData(groupPO.getRecordErrorData());
                    dataCheckExtendDTO.setErrorDataRetentionTime(groupPO.getErrorDataRetentionTime());
                    dataCheckExtendDTOS.add(dataCheckExtendDTO);
                }
                Map<Integer, List<DataCheckExtendDTO>> extend = dataCheckExtendDTOS.stream().collect(groupingBy(i -> i.ruleId));
                dataCheckEditList = dataCheckEditList.stream().map(i -> {
                    List<DataCheckExtendDTO> dataCheckExtendDTOS1 = extend.get(i.getId());
                    if (i.getId() == 0) {
                        DataCheckExtendDTO dataCheckExtendDTO = dataCheckExtendDTOS1 .get(0);
                        DataCheckExtendDTO extendDTO = new DataCheckExtendDTO();
                        BeanUtils.copyProperties(dataCheckExtendDTO, extendDTO,DataCheckExtendDTO.class);
                        extendDTO.fieldName = i.getFieldName();
                        extendDTO.fieldUnique = i.getFieldUnique();
                        i.setDataCheckExtend(extendDTO);
                    } else {
                        if (!CollectionUtils.isEmpty(dataCheckExtendDTOS1)){
                            i.setDataCheckExtend(dataCheckExtendDTOS1.get(0));
                        }
                    }
                    return i;
                }).collect(Collectors.toList());

            }
            dto.setDataCheckList(dataCheckEditList);
            // 同步更新数据校验组下的质量规则
            log.info("editDataCheckByStandards-同步更新数据校验组下的质量规则[{}]", JSONObject.toJSON(dto));
            this.editDataCheckStandardsGroup(dto);
        }
        return ResultEnum.SUCCESS;
    }
}
