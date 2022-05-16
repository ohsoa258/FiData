package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateSceneEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.ITemplateManageService;
import com.fisk.datagovernance.vo.dataquality.template.TemplateVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 模板管理实现类
 * @date 2022/3/23 12:25
 */
@Service
public class TemplateManageImpl extends ServiceImpl<TemplateMapper, TemplatePO> implements ITemplateManageService {
    @Override
    public List<TemplateVO> getAll() {
        List<TemplateVO> templateVOS = new ArrayList<>();
        QueryWrapper<TemplatePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplatePO::getDelFlag, 1);
        List<TemplatePO> templatePOS = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(templatePOS)) {
            return templateVOS;
        }
        for (TemplatePO e : templatePOS) {
            TemplateVO templateVO = new TemplateVO();
            templateVO.setModuleType(ModuleTypeEnum.getEnum(e.getModuleType()));
            templateVO.setModuleName(e.getModuleName());
            templateVO.setTemplateScene(TemplateSceneEnum.getEnum(e.getTemplateScene()));
            templateVO.setSceneDesc(e.getSceneDesc());
            templateVO.setId(Math.toIntExact(e.getId()));
            templateVO.setTemplatenName(e.getTemplatenName());
            templateVO.setTemplateType(TemplateTypeEnum.getEnum(e.getTemplateType()));
            templateVO.setTemplateDesc(e.getTemplateDesc());
            templateVO.setCreateTime(e.getCreateTime());
            templateVO.setCreateUser(e.getCreateUser());
            templateVOS.add(templateVO);
        }
        return templateVOS;
    }
}
