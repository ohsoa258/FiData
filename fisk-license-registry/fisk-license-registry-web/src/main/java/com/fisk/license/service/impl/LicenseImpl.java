package com.fisk.license.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.license.dto.LicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.entity.LicencePO;
import com.fisk.license.mapper.LicenseMapper;
import com.fisk.license.service.ILicenseService;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description LicenseImpl
 * @date 2022/11/10 15:51
 */
@Service
@Slf4j
public class LicenseImpl extends ServiceImpl<LicenseMapper, LicencePO> implements ILicenseService {
    @Override
    public ResultEntity<VerifyLicenceVO> verifyLicenceByUrl(VerifyLicenceDTO dto) {
        VerifyLicenceVO verifyLicenceVO = new VerifyLicenceVO();
        return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, verifyLicenceVO);
    }

    @Override
    public ResultEntity<LicenceVO> createLicence(LicenceDTO dto) {
        LicenceVO licenceVO = new LicenceVO();
        if (dto == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, licenceVO);
        }
        if (StringUtils.isEmpty(dto.getMac()) || StringUtils.isEmpty(dto.getExpireTime()) || CollectionUtils.isEmpty(dto.getMenus())) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, licenceVO);
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, licenceVO);
    }
}
