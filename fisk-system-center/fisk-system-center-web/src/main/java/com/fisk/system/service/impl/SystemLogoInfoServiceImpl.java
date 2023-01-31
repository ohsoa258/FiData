package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.entity.SystemLogoInfoDTO;
import com.fisk.system.entity.SystemLogoInfoPO;
import com.fisk.system.enums.PictureSuffixTypeEnum;
import com.fisk.system.mapper.SystemLogoInfoMapper;
import com.fisk.system.service.SystemLogoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
     * @param title
     * @param file
     * @return
     */
    @Override
    public ResultEnum saveLogoInfo(String title, MultipartFile file) {
        log.info("参数信息： title-【{}】 fileName- 【{}】 logoUrl- 【{}】", title, file, logoUrl);
        if (StringUtils.isEmpty(title)){
            return ResultEnum.SYSTEM_TITLE_NULL;
        }

        if (file.isEmpty()){
            return ResultEnum.SYSTEM_LOGO_NULL;
        }

        mapper.delete(null);

        SystemLogoInfoPO info = new SystemLogoInfoPO();
        info.setLogo(getFileName(file));
        info.setTitle(title);
        // 存储到数据库
        int insert = mapper.insert(info);
        return insert > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    private String getFileName(MultipartFile file){
        //如果文件夹不存在，创建
        File fileP = new File(logoUrl);

        if (!fileP.isDirectory()) {
            //递归生成文件夹
            fileP.mkdirs();
        }
        String fileName = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return null;
        }

        String uuid = UUID.randomUUID().toString();
        if (originalFilename.endsWith(PictureSuffixTypeEnum.JGP.getName())) {
            fileName = String.format("%s.jpg", uuid);
        } else if (originalFilename.endsWith(PictureSuffixTypeEnum.PNG.getName())) {
            fileName = String.format("%s.jpg", uuid);
        } else if (originalFilename.endsWith(PictureSuffixTypeEnum.JPEG.getName())) {
            fileName = String.format("%s.jpeg", uuid);
        } else if (originalFilename.endsWith(PictureSuffixTypeEnum.BMP.getName())) {
            fileName = String.format("%s.bmp", uuid);
        } else {
            throw new FkException(ResultEnum.VISUAL_IMAGE_ERROR);
        }
        log.info("开始存储文件到服务器");

        try {
            file.transferTo(new File(fileP, fileName));
        } catch (IOException e) {
            throw new FkException(ResultEnum.ERROR);
        }
        log.info("结束存储文件到服务器");

        // 图片完整路径
        return "/file/systemlogo/images/" + fileName;
    }

    /**
     * 获取系统logo及系统名称
     * @return
     */
    @Override
    public ResultEntity<Object> getLogoInfo() {
        SystemLogoInfoPO infoPO = mapper.selectOne(null);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, infoPO);
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
        if (StringUtils.isEmpty(systemLogoInfoDTO.getTitle()) && file.isEmpty()){
            return ResultEnum.SUCCESS;
        }
        // 查询数据是否存在
        SystemLogoInfoPO info = mapper.selectById(systemLogoInfoDTO.getId());
        if (info == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        SystemLogoInfoPO po = new SystemLogoInfoPO();
        // 更新数据
        if (!file.isEmpty()){
            po.setLogo(getFileName(file));
        }
        if (StringUtils.isNotEmpty(systemLogoInfoDTO.getTitle())){
            po.setTitle(systemLogoInfoDTO.getTitle());
        }
        po.setId(systemLogoInfoDTO.getId());
        int update = mapper.updateById(po);
        return update > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
