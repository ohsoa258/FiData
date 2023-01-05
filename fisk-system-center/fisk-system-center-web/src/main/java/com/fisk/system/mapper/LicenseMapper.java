package com.fisk.system.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.entity.LicencePO;
import com.fisk.system.vo.license.LicenceVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description LicenseMapper
 * @date 2022/11/10 15:48
 */
@Mapper
public interface LicenseMapper extends FKBaseMapper<LicencePO> {
    List<LicenceVO> getAll();
}
