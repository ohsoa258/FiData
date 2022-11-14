package com.fisk.license.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.LicenseEnCryptUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.license.dto.LicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.entity.LicencePO;
import com.fisk.license.enums.LicenceStateEnum;
import com.fisk.license.mapper.LicenseMapper;
import com.fisk.license.service.ILicenseService;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public VerifyLicenceVO verifyCompanyLicenceByUrl(VerifyLicenceDTO dto) {
        VerifyLicenceVO verifyLicenceVO = new VerifyLicenceVO();
        if (dto == null || StringUtils.isEmpty(dto.getRelativePathUrl())) {
            return verifyLicenceVO;
        }
        try {
            LicencePO licencePO = baseMapper.selectById(1);
            if (licencePO == null || StringUtils.isEmpty(licencePO.getLicence())) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_NONE);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_NONE.getName());
                return verifyLicenceVO;
            }
            if (licencePO.getDelFlag() == 0) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_DISABLED);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_DISABLED.getName());
                return verifyLicenceVO;
            }
            LicenceDTO licenceDTO = decryptCompanyLicense(licencePO.getLicence());
            if (licenceDTO == null) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_DECRYPT_FAIL);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_DECRYPT_FAIL.getName());
                return verifyLicenceVO;
            }
            if (StringUtils.isNotEmpty(dto.getMac())) {
                if (!dto.getMac().equals(licenceDTO.getMac())) {
                    verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_NONE);
                    verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_NONE.getName());
                    return verifyLicenceVO;
                }
            }
            String toDateTime = DateTimeUtils.getNowToShortDate("yyyy/MM/dd");
            String expireTime = licenceDTO.getExpireTime();
            verifyLicenceVO.setLicenceExpireTime(expireTime);
            // 必须为严格的年月日格式，否则无法对比时间 2023/01/01
            int i = toDateTime.compareTo(expireTime);
            if (i > 0) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_EXPIRED);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_EXPIRED.getName());
                return verifyLicenceVO;
            }
            boolean contains = RegexUtils.isContains(licenceDTO.getMenus(), dto.getRelativePathUrl());
            if (contains) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_AUTHORIZED);
            } else {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_UNAUTHORIZED);
            }
        } catch (Exception ex) {
            log.error("【verifyCompanyLicenceByUrl】 ex：" + ex);
            verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_DECRYPT_ERROR);
        }
        verifyLicenceVO.setLicenceStateDescribe(verifyLicenceVO.getLicenceState().getName());
        return verifyLicenceVO;
    }

    @Override
    public ResultEntity<LicenceVO> createCompanyLicence(LicenceDTO dto) {
        LicenceVO licenceVO = new LicenceVO();
        if (dto == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, licenceVO);
        }
        if (StringUtils.isEmpty(dto.getMac()) || StringUtils.isEmpty(dto.getExpireTime()) || CollectionUtils.isEmpty(dto.getMenus())) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, licenceVO);
        }
        try {
            // 第一步：解析参数
            String mac = dto.getMac();
            String menuStr = JSON.toJSONString(dto.getMenus());
            String expireStamp = DateTimeUtils.dateToStamp(dto.getExpireTime());
            String str = mac + "!@#" + menuStr + "!@#" + expireStamp;
            // 第二步：参数加密
            String licence = LicenseEnCryptUtils.encrypt(str);
            // 第三步：解析加密数据
            LicenceDTO licenceDTO = decryptCompanyLicense(licence);
            if (licenceDTO == null) {
                return ResultEntityBuild.buildData(ResultEnum.LICENCE_DECRYPT_FAIL, licenceVO);
            }
            // 第四步：保存数据库
            LicencePO licencePO = new LicencePO();
            licencePO.setLicence(licence);
            int insert = baseMapper.insert(licencePO);
            if (insert > 0) {
                licenceVO.setLicence(licence);
                licenceVO.setLicenceExpireTime(dto.getExpireTime());
            }
        } catch (Exception ex) {
            log.error("【createCompanyLicence】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【createCompanyLicence】 ex：" + ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, licenceVO);
    }

    /**
     * @return com.fisk.license.dto.LicenceDTO
     * @description 解密公司许可证
     * @author dick
     * @date 2022/11/10 22:50
     * @version v1.0
     * @params license
     */
    private LicenceDTO decryptCompanyLicense(String license) {
        LicenceDTO licence = new LicenceDTO();
        try {
            if (StringUtils.isEmpty(license)) {
                return licence;
            }
            String data = LicenseEnCryptUtils.decrypt(license);
            if (StringUtils.isEmpty(data)) {
                return licence;
            }
            String mac = data.split("!@#")[0];
            List<String> menus = JSON.parseArray(data.split("!@#")[1], String.class);
            String expireStamp = data.split("!@#")[2];
            if (StringUtils.isEmpty(mac) || CollectionUtils.isEmpty(menus) || StringUtils.isEmpty(expireStamp)) {
                return licence;
            }
            String expireDate = DateTimeUtils.stampToDate(expireStamp);
            licence.setMac(mac);
            licence.setMenus(menus);
            licence.setExpireTime(expireDate);
        } catch (Exception ex) {
            log.error("【decryptCompanyLicense】 ex：" + ex);
        }
        return licence;
    }
}
