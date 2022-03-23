package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.vo.dataquality.template.TemplateVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 模板管理接口
 * @date 2022/3/23 12:22
 */
public interface ITemplateManageService extends IService<TemplatePO> {
    /**
     * 获取所有模板
     * @return 查询结果
     */
    List<TemplateVO> getAll();
}
