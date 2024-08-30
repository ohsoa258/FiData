package com.fisk.datagovernance.service.impl.dataquality;

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
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service("datacheckStandardsGroupService")
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
     * @param standardsId 标准ID，根据此ID获取相应的标准分组信息
     * @return 返回一个包含数据检查标准分组信息的列表
     */
    @Override
    public PageDTO<DatacheckStandardsGroupVO> getDataCheckStandardsGroup(Integer standardsId, Integer current, Integer size) {
        PageDTO<DatacheckStandardsGroupVO> pageDTO = new PageDTO<>();
        List<DatacheckStandardsGroupVO> groupDtoList = new ArrayList<>();
        // 根据菜单ID获取标准ID列表
        List<Integer> standardByMenuId = dataManageClient.getStandardByMenuId(standardsId);
        if (!CollectionUtils.isEmpty(standardByMenuId)) {
            // 查询条件构造，查询与标准ID列表匹配的所有标准分组信息
            LambdaQueryWrapper<DatacheckStandardsGroupPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DatacheckStandardsGroupPO::getStandardsId, standardByMenuId)
                    .orderByDesc(DatacheckStandardsGroupPO::getCreateTime);
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
            ResultEnum ruleCheckResultEnum = dataCheckManageImpl.batchAddData(dataCheckList);
            if (ruleCheckResultEnum != ResultEnum.SUCCESS) {
                // 质量验证不通过，删除刚刚添加的数据元组
                this.removeById(groupPO.id);
                return ruleCheckResultEnum;
            }
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

                // 如果是FiData的Tree节点，需要将平台数据源ID转换为数据质量数据源ID
                if (i.getSourceType() == SourceTypeEnum.FiData) {
                    int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(i.getSourceType(), i.getDatasourceId());
                    if (idByDataSourceId != 0 && i.getId() != 0) {
                        i.setDatasourceId(idByDataSourceId);
                    }
                }
                return i;
            }).collect(Collectors.toList());
            List<Integer> dataCheckIds = dataCheckEditList.stream().map(i -> i.getId()).collect(Collectors.toList());
            LambdaQueryWrapper<DataCheckPO> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DataCheckPO::getDatacheckGroupId, groupPO.id);
            queryWrapper1.eq(DataCheckPO::getTemplateId, templateId);
            List<DataCheckPO> dataCheckPOS = dataCheckManageService.list(queryWrapper1);
            for (DataCheckPO dataCheckPO : dataCheckPOS) {
                if (!dataCheckIds.contains((int) dataCheckPO.id)) {
                    dataCheckManageService.deleteData((int) dataCheckPO.id);
                }
            }
            ResultEnum ruleCheckResultEnum = dataCheckManageImpl.batchEditData(dataCheckEditList);
            if (ruleCheckResultEnum != ResultEnum.SUCCESS) {
                return ruleCheckResultEnum;
            }
        }else {
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
        Set<Integer> dbIds = standardsBeCitedDTOList.stream().map(StandardsBeCitedDTO::getDbId).collect(Collectors.toSet());
        Map<Integer, Integer> idByDataSourceIds = dataSourceConManageImpl.getIdByDataSourceIds(SourceTypeEnum.FiData, dbIds);
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
            if(!CollectionUtils.isEmpty(standardsBeCitedDTOList)) {
                for (StandardsBeCitedDTO standardsBeCitedDTO : standardsBeCitedDTOList) {
                    Integer dbId = idByDataSourceIds.get(standardsBeCitedDTO.getDbId());
                    DataCheckEditDTO checkEditDTO = new DataCheckEditDTO();
                    DataCheckPO dataCheckPO1 = dataCheckPOList.get(0);
                    checkEditDTO.setDatacheckGroupId((int) groupPO.id);
                    checkEditDTO.setRuleCheckType(RuleCheckTypeEnum.getEnum(dataCheckPO1.getRuleCheckType()));
                    checkEditDTO.setDatasourceId(standardsBeCitedDTO.getDbId());
                    checkEditDTO.setRuleDescribe(dataCheckPO1.getRuleDescribe());
                    checkEditDTO.setRuleExecuteNode(RuleExecuteNodeTypeEnum.getEnum(dataCheckPO1.getRuleExecuteNode()));
                    checkEditDTO.setRuleIllustrate(dataCheckPO1.getRuleIllustrate());
                    checkEditDTO.setTableBusinessType(standardsBeCitedDTO.getTableBusinessType());
                    checkEditDTO.setRuleName(groupPO.getCheckGroupName() + standardsBeCitedDTO.getTableName() + standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setRuleState(RuleStateEnum.Enable);
                    checkEditDTO.setRuleWeight(dataCheckPO1.getRuleWeight());
                    checkEditDTO.setTableDescribe(standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setSchemaName(standardsBeCitedDTO.getSchemaName());
                    checkEditDTO.setTableUnique(standardsBeCitedDTO.getTableId());
                    checkEditDTO.setTableName(standardsBeCitedDTO.getTableName());
                    checkEditDTO.setFieldUnique(standardsBeCitedDTO.getFieldId());
                    checkEditDTO.setFieldName(standardsBeCitedDTO.getFieldName());
                    checkEditDTO.setTableType(TableTypeEnum.TABLE);
                    checkEditDTO.setTemplateId(templateVOList.get(0).id);
                    checkEditDTO.setSourceType(SourceTypeEnum.FiData);
                    checkEditDTO.setRuleExecuteSort(dataCheckPO1.getRuleExecuteSort());
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
                    dataCheckEditList.add(checkEditDTO);
                }
                List<Integer> ruleIds = dataCheckEditList.stream().map(DataCheckEditDTO::getId).collect(Collectors.toList());
                LambdaQueryWrapper<DataCheckExtendPO> queryWrapper2 = new LambdaQueryWrapper<>();
                queryWrapper2.in(DataCheckExtendPO::getRuleId, ruleIds);
                List<DataCheckExtendPO> dataCheckExtendPOS = dataCheckExtendMapper.selectList(queryWrapper2);
                List<DataCheckExtendDTO> dataCheckExtendDTOS = DataCheckExtendMap.INSTANCES.poListToDtoList(dataCheckExtendPOS);
                Map<Integer, List<DataCheckExtendDTO>> extend = dataCheckExtendDTOS.stream().collect(groupingBy(i -> i.ruleId));
                dataCheckEditList = dataCheckEditList.stream().map(i -> {
                    DataCheckExtendDTO dataCheckExtendDTO = new DataCheckExtendDTO();
                    List<DataCheckExtendDTO> dataCheckExtendDTOS1 = extend.get(i.getId());
                    if (CollectionUtils.isEmpty(dataCheckExtendDTOS1)) {
                        for (List<DataCheckExtendDTO> value : extend.values()) {
                            dataCheckExtendDTO = value.get(0);
                            break;
                        }
                        dataCheckExtendDTO.fieldName = i.getFieldName();
                        dataCheckExtendDTO.fieldUnique = i.getFieldUnique();
                        i.setDataCheckExtend(dataCheckExtendDTO);
                    } else {
                        i.setDataCheckExtend(dataCheckExtendDTOS1.get(0));
                    }
                    return i;
                }).collect(Collectors.toList());
            }
            dto.setDataCheckList(dataCheckEditList);
            this.editDataCheckStandardsGroup(dto);
        }
        return ResultEnum.SUCCESS;
    }
}
