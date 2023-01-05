package com.fisk.license.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.HardWareUtils;
import com.fisk.common.core.utils.LicenseEnCryptUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.license.LicenceDTO;
import com.fisk.license.dto.AuthorizeLicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.entity.LicencePO;
import com.fisk.license.enums.LicenceStateEnum;
import com.fisk.license.mapper.LicenseMapper;
import com.fisk.license.service.ILicenseService;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.ServiceRegistryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description LicenseImpl
 * @date 2022/11/10 15:51
 */
@Service
@Slf4j
public class LicenseImpl extends ServiceImpl<LicenseMapper, LicencePO> implements ILicenseService {

    @Resource
    private UserClient userClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEntity<String> setCompanyLicence(AuthorizeLicenceDTO dto) {
        if (dto == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, "");
        }
        if (StringUtils.isEmpty(dto.getLicence())) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, "");
        }
        try {
            LicenceVO licenceVO = decryptCompanyLicense(dto.getLicence());
            if (licenceVO == null) {
                return ResultEntityBuild.buildData(ResultEnum.LICENCE_DECRYPT_FAIL, "");
            }
            String localMacAddress = HardWareUtils.getMacAddress();
            log.info("【authorizeCompanyLicence】sys mac：" + localMacAddress);
            log.info("【authorizeCompanyLicence】db mac：" + licenceVO.getMac());
            if (!localMacAddress.equals(licenceVO.getMac())) {
                log.error("【setCompanyLicence】Mac解析后与当前计算机Mac不匹配");
                return ResultEntityBuild.buildData(ResultEnum.MAC_DECRYPT_FAIL, "");
            }
            // 过期时间
            String expireTime = licenceVO.getExpireTime();
            // 当前时间
            String toDateTime = DateTimeUtils.getNowToShortDate("yyyy/MM/dd");
            // 必须为严格的年月日格式，否则无法对比时间 2023/01/01
            int i = toDateTime.compareTo(expireTime);
            if (i > 0) {
                return ResultEntityBuild.buildData(ResultEnum.LICENCE_EXPIRED, "");
            }
            baseMapper.updateDelFlag();
            LicencePO licencePO = new LicencePO();
            licencePO.setLicence(dto.licence);
            baseMapper.insert(licencePO);
        } catch (Exception ex) {
            log.error("【authorizeCompanyLicence】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【authorizeCompanyLicence】 ex：" + ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "授权成功");
    }

    @Override
    public VerifyLicenceVO verifyCompanyLicenceByUrl(VerifyLicenceDTO dto) {
        VerifyLicenceVO verifyLicenceVO = new VerifyLicenceVO();
        if (dto == null || StringUtils.isEmpty(dto.getRelativePathUrl())) {
            return verifyLicenceVO;
        }
        try {
            // 查询单条有效的
            QueryWrapper<LicencePO> licencePOQueryWrapper = new QueryWrapper<>();
            licencePOQueryWrapper.lambda().eq(LicencePO::getDelFlag, 1);
            LicencePO licencePO = baseMapper.selectOne(licencePOQueryWrapper);
            if (licencePO == null || StringUtils.isEmpty(licencePO.getLicence())) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_NONE);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_NONE.getName());
                return verifyLicenceVO;
            }
            LicenceVO licenceVO = decryptCompanyLicense(licencePO.getLicence());
            if (licenceVO == null) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_DECRYPT_FAIL);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_DECRYPT_FAIL.getName());
                return verifyLicenceVO;
            }
            String localMacAddress = HardWareUtils.getMacAddress();
            log.info("【verifyCompanyLicenceByUrl】sys mac：" + localMacAddress);
            log.info("【verifyCompanyLicenceByUrl】db mac：" + licenceVO.getMac());
            if (!localMacAddress.equals(licenceVO.getMac())) {
                log.error("【verifyCompanyLicenceByUrl】Mac解析后与当前计算机Mac不匹配");
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_DECRYPT_ERROR);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_DECRYPT_ERROR.getName());
                return verifyLicenceVO;
            }
            String toDateTime = DateTimeUtils.getNowToShortDate("yyyy/MM/dd");
            String expireTime = licenceVO.getExpireTime();
            verifyLicenceVO.setLicenceExpireTime(expireTime);
            // 必须为严格的年月日格式，否则无法对比时间 2023/01/01
            int i = toDateTime.compareTo(expireTime);
            if (i > 0) {
                verifyLicenceVO.setLicenceState(LicenceStateEnum.LICENCE_EXPIRED);
                verifyLicenceVO.setLicenceStateDescribe(LicenceStateEnum.LICENCE_EXPIRED.getName());
                return verifyLicenceVO;
            }
            boolean contains = RegexUtils.isContains(licenceVO.getMenus(), dto.getRelativePathUrl());
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
    public ResultEntity<LicenceVO> getCompanyLicence() {
        LicenceVO licenceVO;
        try {
            // 查询单条有效的
            QueryWrapper<LicencePO> licencePOQueryWrapper = new QueryWrapper<>();
            licencePOQueryWrapper.lambda().eq(LicencePO::getDelFlag, 1);
            LicencePO licencePO = baseMapper.selectOne(licencePOQueryWrapper);
            if (licencePO == null || StringUtils.isEmpty(licencePO.getLicence())) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
            }
            licenceVO = decryptCompanyLicense(licencePO.getLicence());
            if (licenceVO == null) {
                return ResultEntityBuild.buildData(ResultEnum.LICENCE_DECRYPT_FAIL, null);
            }
        } catch (Exception ex) {
            log.error("【getCompanyLicence】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【getCompanyLicence】 ex：" + ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, licenceVO);
    }

    @Override
    public ResultEntity<String> getMacAddress() {
        String localMacAddress = "";
        try {
            localMacAddress = HardWareUtils.getMacAddress();
            log.info("【getMacAddress】mac：" + localMacAddress);
        } catch (Exception ex) {
            log.error("【getMacAddress】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【getMacAddress】 ex：" + ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, localMacAddress);
    }

    /**
     * @return com.fisk.license.vo.LicenceVO
     * @description 解密公司许可证
     * @author dick
     * @date 2022/11/10 22:50
     * @version v1.0
     * @params license
     */
    private LicenceVO decryptCompanyLicense(String license) {
        LicenceVO licence = new LicenceVO();
        try {
            if (StringUtils.isEmpty(license)) {
                return null;
            }
            String dataStr = LicenseEnCryptUtils.decrypt(license);
            if (StringUtils.isEmpty(dataStr)) {
                return null;
            }
            List<String> data = Arrays.stream(dataStr.split("!@#")).collect(Collectors.toList());
            if (data.size() != 6) {
                return null;
            }
            String platform = data.get(0);
            String authorizer = data.get(1);
            String mac = data.get(2);
            List<String> menus = JSON.parseArray(data.get(3), String.class);
            String expireStamp = data.get(4);
            String authDateStamp = data.get(5);
            if (StringUtils.isEmpty(platform) ||
                    StringUtils.isEmpty(authorizer) ||
                    StringUtils.isEmpty(mac) ||
                    CollectionUtils.isEmpty(menus) ||
                    StringUtils.isEmpty(expireStamp) ||
                    StringUtils.isEmpty(authDateStamp)) {
                return null;
            }
            // 查询菜单url对应的名称
            ResultEntity<Object> clientMenuList = userClient.getMenuList();
            if (clientMenuList == null || clientMenuList.getData() == null) {
                return null;
            }
            List<String> menuNameList = new ArrayList<>();
            String json = JSONArray.toJSON(clientMenuList.getData()).toString();
            List<ServiceRegistryDTO> menuList = JSONArray.parseArray(json, ServiceRegistryDTO.class);
            menuList.forEach(t -> {
                if (CollectionUtils.isNotEmpty(t.getDtos())) {
                    t.getDtos().forEach(next -> {
                        String menuAddress = String.format("/%s/%s", t.getServeUrl(), next.getServeUrl());
                        if (RegexUtils.isContains(menus, menuAddress)) {
                            menuNameList.add(next.getServeCnName());
                        }
                    });
                }
            });
            String expireDate = DateTimeUtils.stampToDate(expireStamp);
            String authDate = DateTimeUtils.stampToDate(authDateStamp);
            licence.setPlatform(platform);
            licence.setAuthorizer(authorizer);
            licence.setMac(mac);
            licence.setMenus(menus);
            licence.setMenuNames(menuNameList);
            licence.setExpireTime(expireDate);
            licence.setAuthDate(authDate);
            licence.setLicence(license);
        } catch (Exception ex) {
            log.error("【decryptCompanyLicense】 ex：" + ex);
            return null;
        }
        return licence;
    }

}
