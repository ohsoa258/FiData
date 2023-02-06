package com.fisk.system.service.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.SystemLogoInfoDTO;
import com.fisk.system.entity.SystemLogoInfoPO;
import com.fisk.system.mapper.SystemLogoInfoMapper;
import com.fisk.system.service.SystemLogoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName:
 * @Author: SonJianJian
 * @Date: 2023
 * @Copyright: 2023 by SongJianJian
 * @Description:
 **/
@Service
@Slf4j
public class SystemLogoInfoServiceImpl implements SystemLogoInfoService {

    @Value("${file.logoUrl}")
    private String logoUrl;

    @Resource
    private SystemLogoInfoMapper mapper;

    /**
     * 存储系统logo及系统名称
     *
     * @param systemLogoInfoDTO
     * @param file
     * @return
     */
    @Override
    public ResultEnum saveLogoInfo(SystemLogoInfoDTO systemLogoInfoDTO, MultipartFile file) {
        log.info("参数信息： title-【{}】 fileName- 【{}】 logoUrl- 【{}】", systemLogoInfoDTO.getTitle(), file, logoUrl);
        if (StringUtils.isEmpty(systemLogoInfoDTO.getTitle())){
            return ResultEnum.SYSTEM_TITLE_NULL;
        }

        if (file.isEmpty()){
            return ResultEnum.SYSTEM_LOGO_NULL;
        }

        mapper.delete(null);

        String fileStr = "";
        // 通过base64来转化图片
        try {
            fileStr = Base64.encodeBase64String(file.getBytes());
            fileStr = fileStr.replaceAll("[\\s*\t\n\r]", "");
        } catch (IOException e) {
            log.error("文件转码出错", e);
            return ResultEnum.SAVE_DATA_ERROR;
        }

        systemLogoInfoDTO.setLogo(fileStr);
        // 存储到数据库
        int insert = mapper.insert(systemLogoInfoDTO);
        return insert > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 获取系统logo及系统名称
     * @return
     */
    @Override
    public ResultEntity<Object> getLogoInfo() {
        SystemLogoInfoDTO info = mapper.selectOne(null);
        info.setLogo("data:image/png;base64," + info.getLogo());
        return ResultEntityBuild.build(ResultEnum.SUCCESS, info);
    }

    /**
     * 更新系统logo及系统名称
     *
     * @param systemLogoInfoDTO
     * @param file
     * @return
     */
    @Override
    public ResultEnum updateLogoInfo(SystemLogoInfoDTO systemLogoInfoDTO, MultipartFile file) {
        if (systemLogoInfoDTO.getId() == null || systemLogoInfoDTO.getId() <= 0){
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 查询数据是否存在
        SystemLogoInfoDTO info = mapper.selectById(systemLogoInfoDTO.getId());
        if (info == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 文件不为空则处理文件
        if (file != null && file.getSize() != 0){
            String fileStr = "";
            // 通过base64来转化图片
            try {
                fileStr = Base64.encodeBase64String(file.getBytes());
                fileStr = fileStr.replaceAll("[\\s*\t\n\r]", "");
            } catch (IOException e) {
                log.error("文件转码出错", e);
                return ResultEnum.SAVE_DATA_ERROR;
            }
            info.setLogo(fileStr);
        }
        // 标题不为空，则设置标题
        if (StringUtils.isNotEmpty(systemLogoInfoDTO.getTitle())){
            info.setTitle(systemLogoInfoDTO.getTitle());
        }
        if (StringUtils.isNotEmpty(systemLogoInfoDTO.getColor())){
            info.setColor(systemLogoInfoDTO.getColor());
        }
        if (StringUtils.isNotEmpty(systemLogoInfoDTO.getSize())){
            info.setSize(systemLogoInfoDTO.getSize());
        }
        if (StringUtils.isNotEmpty(systemLogoInfoDTO.getFontFamily())){
            info.setFontFamily(systemLogoInfoDTO.getFontFamily());
        }
        if (systemLogoInfoDTO.getOverStriking() != null){
            info.setOverStriking(systemLogoInfoDTO.getOverStriking());
        }

        int update = mapper.updateById(info);
        return update > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
