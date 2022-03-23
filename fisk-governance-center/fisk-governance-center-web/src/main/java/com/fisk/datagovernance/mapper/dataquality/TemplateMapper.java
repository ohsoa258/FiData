package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 模板管理
 * @date 2022/3/23 12:26
 */
@Mapper
public interface TemplateMapper extends FKBaseMapper<TemplatePO> {

}
