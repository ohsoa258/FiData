package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DatacheckStandardsGroupDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.entity.dataquality.DatacheckStandardsGroupPO;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.map.dataquality.DatacheckStandardsGroupMap;
import com.fisk.datagovernance.mapper.dataquality.DataCheckExtendMapper;
import com.fisk.datagovernance.mapper.dataquality.DatacheckStandardsGroupMapper;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.service.dataquality.IDatacheckStandardsGroupService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DatacheckStandardsGroupVO;
import com.fisk.datamanage.client.DataManageClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("datacheckStandardsGroupService")
public class DatacheckStandardsGroupServiceImpl extends ServiceImpl<DatacheckStandardsGroupMapper, DatacheckStandardsGroupPO> implements IDatacheckStandardsGroupService {

    @Resource
    DataManageClient dataManageClient;

    @Resource
    IDataCheckManageService dataCheckManageService;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;


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
            queryWrapper.in(DatacheckStandardsGroupPO::getStandardsId, standardByMenuId);
            List<DatacheckStandardsGroupPO> groupPOList = this.list(queryWrapper);
            // 如果查询结果为空，直接返回空列表
            if (CollectionUtils.isEmpty(groupPOList)) {
                return pageDTO;
            }
            // 将PO对象列表转换为VO对象列表
            current = current - 1;
            groupDtoList = groupPOList.stream().map(DatacheckStandardsGroupMap.INSTANCES::poToVo)
                    .skip((current - 1 + 1) * size).limit(size).collect(Collectors.toList());
            pageDTO.setTotal(Long.valueOf(groupDtoList.size()));
            pageDTO.setTotalPage((long) Math.ceil(1.0 * groupDtoList.size() / size));

            // 根据分组ID列表获取所有规则信息
            List<Integer> groupIds = groupPOList.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            List<DataCheckVO> allRules = dataCheckManageService.getRuleByIds(groupIds);

            // 如果规则信息为空，直接返回已构造的标准分组VO列表
            if (CollectionUtils.isEmpty(allRules)) {
                return pageDTO;
            }
            // 获取所有规则ID的列表，去重后查询扩展信息
            List<Integer> ruleIds = allRules.stream().map(DataCheckVO::getId).distinct().collect(Collectors.toList());
            List<DataCheckExtendVO> dataCheckExtendVOList = dataCheckExtendMapper.getDataCheckExtendByRuleIdList(ruleIds);
            // 如果存在扩展信息，则为每条规则设置扩展信息
            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(dataCheckExtendVOList)) {
                allRules.forEach(t -> {
                    DataCheckExtendVO dataCheckExtendVO = dataCheckExtendVOList.stream().filter(k -> k.getRuleId() == t.getId()).findFirst().orElse(null);
                    t.setDataCheckExtend(dataCheckExtendVO);
                });
            }
            // 根据指定的排序规则对规则信息进行排序
            allRules = allRules.stream().sorted(
                    Comparator.comparing(DataCheckVO::getTableAlias, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(DataCheckVO::getRuleExecuteNode, Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(DataCheckVO::getTemplateType, Comparator.nullsFirst(Comparator.naturalOrder()))
            ).collect(Collectors.toList());
            // 根据数据检查组ID将规则信息分组，然后为每个标准分组VO设置规则信息列表
            if (!CollectionUtils.isEmpty(allRules)) {
                Map<Integer, List<DataCheckVO>> datacheckMap = allRules.stream()
                        .collect(Collectors.groupingBy(DataCheckVO::getDatacheckGroupId));
                groupDtoList = groupDtoList.stream().map(i -> {
                    List<DataCheckVO> dataCheckDTOS = datacheckMap.get(i.getId());
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
                i.ruleName = groupPO.getCheckGroupName() + i.tableName;
                return i;
            }).collect(Collectors.toList());
            dataCheckList.forEach(dataCheckDTO -> {
                dataCheckManageService.addData(dataCheckDTO);
            });
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
        queryWrapper.eq(DatacheckStandardsGroupPO::getStandardsId, dto.getStandardsId());
        List<DatacheckStandardsGroupPO> group = this.list(queryWrapper);
        List<String> groupNames = group.stream().map(DatacheckStandardsGroupPO::getCheckGroupName).collect(Collectors.toList());
        List<String> names = groupNames.stream().filter(i -> i.contains(dto.getCheckGroupName())).collect(Collectors.toList());
        if (names.size()>1) {
            throw new FkException(ResultEnum.CHECK_STANDARDS_GROUP_ERROR);
        }
        this.updateById(groupPO);
        List<DataCheckEditDTO> dataCheckEditList = dto.getDataCheckList();
        if (!CollectionUtils.isEmpty(dataCheckEditList)) {
            dataCheckEditList = dataCheckEditList.stream().map(i -> {
                Integer id = (int) groupPO.id;
                i.setDatacheckGroupId(id);
                i.ruleName = groupPO.getCheckGroupName() + i.tableName;
                return i;
            }).collect(Collectors.toList());
            dataCheckEditList.forEach(dataCheckDTO -> {
                dataCheckManageService.editData(dataCheckDTO);
            });
        }
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


}
