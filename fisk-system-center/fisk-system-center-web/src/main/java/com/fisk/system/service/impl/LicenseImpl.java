package com.fisk.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.HardWareUtils;
import com.fisk.common.core.utils.LicenseEnCryptUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.ServiceRegistryDTO;
import com.fisk.system.dto.license.LicenceDTO;
import com.fisk.system.dto.license.MenuDTO;
import com.fisk.system.entity.EmailServerPO;
import com.fisk.system.entity.LicencePO;
import com.fisk.system.entity.ServiceRegistryPO;
import com.fisk.system.map.ServiceRegistryMap;
import com.fisk.system.mapper.LicenseMapper;
import com.fisk.system.mapper.ServiceRegistryMapper;
import com.fisk.system.service.ILicenseService;
import com.fisk.system.vo.license.LicenceVO;
import com.fisk.system.vo.license.QueryLicenceVO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.apache.bcel.generic.RET;
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
    private UserHelper userHelper;

    @Resource
    private ServiceRegistryMapper serviceRegistryMapper;

    @Override
    public ResultEntity<QueryLicenceVO> getCompanyLicence(String keyWord) {
        return null;
    }

    @Override
    public ResultEnum addCompanyLicence(LicenceDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getMenuList())) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {

            QueryWrapper<LicencePO> licencePOQueryWrapper = new QueryWrapper<>();
            licencePOQueryWrapper.lambda().eq(LicencePO::getCustomerName, dto.getCustomerName())
                    .eq(LicencePO::getDelFlag, 1);
            List<LicencePO> licencePOS = baseMapper.selectList(licencePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(licencePOS)) {
                return ResultEnum.CUSTOMER_ALREADY_EXISTS;
            }

            // mac地址
            String mac = dto.getMachineKey();
            // 平台
            String platform = dto.getCustomerName();
            // 授权人
            String authorizer = userHelper.getLoginUserInfo().getUsername();
            // 菜单url
            List<String> menuList = dto.getMenuList().stream().map(MenuDTO::getName).collect(Collectors.toList());
            String menuStr = JSON.toJSONString(menuList);
            // 过期时间
            String expireStamp = DateTimeUtils.dateToStamp(dto.getExpirationDate());
            // 授权时间
            String authDateStamp = DateTimeUtils.dateToStamp(DateTimeUtils.getNowToShortDate("yyyy/MM/dd"));
            // 参数拼接
            String str = platform + "!@#" + authorizer + "!@#" + mac + "!@#" + menuStr + "!@#" + expireStamp + "!@#" + authDateStamp;

            // 第二步：参数加密
            String licence = LicenseEnCryptUtils.encrypt(str);
            // 第三步：解析加密数据
            LicenceVO licenceVO = decryptCompanyLicense(licence);
            if (licenceVO == null) {
                return ResultEnum.LICENCE_DECRYPT_FAIL;
            }
            licence = licence.replaceAll("[\\s*\t\n\r]", "");

            LicencePO licencePO = new LicencePO();
            licencePO.setCustomerCode(dto.getCustomerCode());
            licencePO.setCustomerName(dto.getCustomerName());
            licencePO.setCustomerLicense(licence);
            licencePO.setMachineKey(dto.getMachineKey());
            List<Integer> menuIdList = dto.getMenuList().stream().map(MenuDTO::getId).collect(Collectors.toList());
            licencePO.setServicesScope(Joiner.on(",").join(menuIdList));
            licencePO.setExpirationDate(dto.getExpirationDate());
            if (baseMapper.insert(licencePO) > 0) {
                return ResultEnum.SUCCESS;
            }
        } catch (Exception ex) {
            log.error("【addCompanyLicence】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【addCompanyLicence】 ex：" + ex);
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum editCompanyLicence(LicenceDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getMenuList())) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {

            QueryWrapper<LicencePO> licencePOQueryWrapper = new QueryWrapper<>();
            licencePOQueryWrapper.lambda().eq(LicencePO::getCustomerName, dto.getCustomerName())
                    .ne(LicencePO::getId, dto.getId())
                    .eq(LicencePO::getDelFlag, 1);
            List<LicencePO> licencePOS = baseMapper.selectList(licencePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(licencePOS)) {
                return ResultEnum.CUSTOMER_ALREADY_EXISTS;
            }

            // mac地址
            String mac = dto.getMachineKey();
            // 平台
            String platform = dto.getCustomerName();
            // 授权人
            String authorizer = userHelper.getLoginUserInfo().getUsername();
            // 菜单url
            List<String> menuList = dto.getMenuList().stream().map(MenuDTO::getName).collect(Collectors.toList());
            String menuStr = JSON.toJSONString(menuList);
            // 过期时间
            String expireStamp = DateTimeUtils.dateToStamp(dto.getExpirationDate());
            // 授权时间
            String authDateStamp = DateTimeUtils.dateToStamp(DateTimeUtils.getNowToShortDate("yyyy/MM/dd"));
            // 参数拼接
            String str = platform + "!@#" + authorizer + "!@#" + mac + "!@#" + menuStr + "!@#" + expireStamp + "!@#" + authDateStamp;

            // 第二步：参数加密
            String licence = LicenseEnCryptUtils.encrypt(str);
            // 第三步：解析加密数据
            LicenceVO licenceVO = decryptCompanyLicense(licence);
            if (licenceVO == null) {
                return ResultEnum.LICENCE_DECRYPT_FAIL;
            }
            licence = licence.replaceAll("[\\s*\t\n\r]", "");

            LicencePO licencePO = new LicencePO();
            licencePO.setCustomerCode(dto.getCustomerCode());
            licencePO.setCustomerName(dto.getCustomerName());
            licencePO.setCustomerLicense(licence);
            licencePO.setMachineKey(dto.getMachineKey());
            List<Integer> menuIdList = dto.getMenuList().stream().map(MenuDTO::getId).collect(Collectors.toList());
            licencePO.setServicesScope(Joiner.on(",").join(menuIdList));
            licencePO.setExpirationDate(dto.getExpirationDate());
            if (baseMapper.insert(licencePO) > 0) {
                return ResultEnum.SUCCESS;
            }
        } catch (Exception ex) {
            log.error("【editCompanyLicence】 ex：" + ex);
            throw new FkException(ResultEnum.ERROR, "【editCompanyLicence】 ex：" + ex);
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteCompanyLicence(int id) {
        LicencePO licencePO = baseMapper.selectById(id);
        if (licencePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return baseMapper.deleteByIdWithFill(licencePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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
            List<String> menuNameList = new ArrayList<>();
            List<ServiceRegistryDTO> menuList = getMenuList();
            menuList.forEach(t -> {
                String menuAddress = String.format("/%s/%s", t.getServeUrl(), next.getServeUrl());
                if (RegexUtils.isContains(menus, menuAddress)) {
                    menuNameList.add(next.getServeCnName());
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
