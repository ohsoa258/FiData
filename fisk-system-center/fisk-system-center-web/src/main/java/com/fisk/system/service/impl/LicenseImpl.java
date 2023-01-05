package com.fisk.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.LicenseEnCryptUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.LoginServiceDTO;
import com.fisk.system.dto.license.LicenceDTO;
import com.fisk.system.dto.license.MenuDTO;
import com.fisk.system.entity.LicencePO;
import com.fisk.system.mapper.LicenseMapper;
import com.fisk.system.service.ILicenseService;
import com.fisk.system.vo.license.LicenceVO;
import com.fisk.system.vo.license.QueryLicenceVO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private RoleServiceAssignmentImpl roleServiceAssignment;

    @Override
    public ResultEntity<QueryLicenceVO> getCompanyLicence(String keyWord) {
        QueryLicenceVO licenceVO = new QueryLicenceVO();
        List<LicenceVO> licenceVOS = baseMapper.getAll();
        licenceVO.setLicenceList(licenceVOS);
        List<LoginServiceDTO> allMenuList = roleServiceAssignment.getAllMenuList();
        licenceVO.setMenuList(allMenuList);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, licenceVO);
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
            // 授权人
            String authorizer = userHelper.getLoginUserInfo().getUsername();
            // 菜单url
            List<String> menuList = dto.getMenuList().stream().map(MenuDTO::getName).collect(Collectors.toList());
            String menuStr = JSON.toJSONString(menuList);
            // 过期时间
            String expireStamp = DateTimeUtils.dateToStamp(dto.getExpirationDate());
            // 授权时间
            String authDate = DateTimeUtils.getNowToShortDate("yyyy/MM/dd");
            String authDateStamp = DateTimeUtils.dateToStamp(authDate);
            // 参数拼接
            String str = dto.getCustomerName() + "!@#" + authorizer + "!@#" + dto.getMachineKey() + "!@#" + menuStr + "!@#" + expireStamp + "!@#" + authDateStamp;

            // 第二步：参数加密
            String licence = LicenseEnCryptUtils.encrypt(str);
            licence = licence.replaceAll("[\\s*\t\n\r]", "");

            LicencePO licencePO = new LicencePO();
            licencePO.setCustomerCode(dto.getCustomerCode());
            licencePO.setCustomerName(dto.getCustomerName());
            licencePO.setCustomerLicense(licence);
            licencePO.setMachineKey(dto.getMachineKey());
            List<Integer> menuIdList = dto.getMenuList().stream().map(MenuDTO::getId).collect(Collectors.toList());
            licencePO.setServicesScope(Joiner.on(",").join(menuIdList));
            licencePO.setExpirationDate(dto.getExpirationDate());
            licencePO.setAuthorizationDate(authDate);
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

            // 授权人
            String authorizer = userHelper.getLoginUserInfo().getUsername();
            // 菜单url
            List<String> menuList = dto.getMenuList().stream().map(MenuDTO::getName).collect(Collectors.toList());
            String menuStr = JSON.toJSONString(menuList);
            // 过期时间
            String expireStamp = DateTimeUtils.dateToStamp(dto.getExpirationDate());
            // 授权时间
            String authDate = DateTimeUtils.getNowToShortDate("yyyy/MM/dd");
            String authDateStamp = DateTimeUtils.dateToStamp(authDate);
            // 参数拼接
            String str = dto.getCustomerName() + "!@#" + authorizer + "!@#" + dto.getMachineKey() + "!@#" + menuStr + "!@#" + expireStamp + "!@#" + authDateStamp;

            // 第二步：参数加密
            String licence = LicenseEnCryptUtils.encrypt(str);
            licence = licence.replaceAll("[\\s*\t\n\r]", "");

            LicencePO licencePO = new LicencePO();
            licencePO.setCustomerCode(dto.getCustomerCode());
            licencePO.setCustomerName(dto.getCustomerName());
            licencePO.setCustomerLicense(licence);
            licencePO.setMachineKey(dto.getMachineKey());
            List<Integer> menuIdList = dto.getMenuList().stream().map(MenuDTO::getId).collect(Collectors.toList());
            licencePO.setServicesScope(Joiner.on(",").join(menuIdList));
            licencePO.setExpirationDate(dto.getExpirationDate());
            licencePO.setAuthorizationDate(authDate);
            if (baseMapper.updateById(licencePO) > 0) {
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
}
