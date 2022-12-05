package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckQueryDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.DataCheckExtendMap;
import com.fisk.datagovernance.mapper.dataquality.DataCheckExtendMapper;
import com.fisk.datagovernance.service.dataquality.IDataCheckExtendManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展属性
 * @date 2022/4/2 11:23
 */
@Service
public class DataCheckExtendManageImpl extends ServiceImpl<DataCheckExtendMapper, DataCheckExtendPO> implements IDataCheckExtendManageService {

    @Override
    public List<DataCheckVO> setTableFieldName(List<DataCheckVO> source, List<FiDataMetaDataDTO> tableFields) {
        List<Integer> ruleIds = source.stream().map(DataCheckVO::getId).collect(Collectors.toList());
        QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
        dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                .in(DataCheckExtendPO::getRuleId, ruleIds);
        List<DataCheckExtendPO> dataCheckExtends = baseMapper.selectList(dataCheckExtendPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckExtends)) {
            for (DataCheckVO ruleDto : source) {
                FiDataMetaDataTreeDTO f_table = null;
                if (ruleDto.getSourceTypeEnum() == SourceTypeEnum.FiData && CollectionUtils.isNotEmpty(tableFields)) {
                    FiDataMetaDataDTO fiDataMetaDataDTO = tableFields.stream().filter(t -> t.getDataSourceId() == ruleDto.getFiDataSourceId()).findFirst().orElse(null);
                    if (fiDataMetaDataDTO != null && CollectionUtils.isNotEmpty(fiDataMetaDataDTO.getChildren())) {
                        f_table = fiDataMetaDataDTO.getChildren().stream().filter(t -> t.getId().equals(ruleDto.getTableUnique())).findFirst().orElse(null);
                    }
                }
                if (f_table != null) {
                    ruleDto.setTableName(f_table.getLabel());
                    ruleDto.setTableAlias(f_table.getLabelAlias());
                } else {
                    ruleDto.setTableName(ruleDto.getTableUnique());
                    ruleDto.setTableAlias(ruleDto.getTableUnique());
                }
                List<DataCheckExtendPO> fieldRules = dataCheckExtends.stream().filter(t -> t.getRuleId() == ruleDto.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(fieldRules)) {
                    FiDataMetaDataTreeDTO finalF_table = f_table;
                    ruleDto.setDataCheckExtends(DataCheckExtendMap.INSTANCES.poToVo(fieldRules));
                    ruleDto.getDataCheckExtends().forEach(t -> {
                        if (finalF_table != null) {
                            FiDataMetaDataTreeDTO f_field = finalF_table.getChildren().stream().filter(field -> field.getId().equals(t.getFieldUnique())).findFirst().orElse(null);
                            if (f_field != null) {
                                t.setFieldName(f_field.getLabel());
                                t.setFieldAlias(f_field.getLabelAlias());
                            }
                        } else {
                            t.setFieldName(t.getFieldUnique());
                            t.setFieldAlias(t.getFieldUnique());
                        }
                    });
                }
            }
        }
        return source;
    }

}
