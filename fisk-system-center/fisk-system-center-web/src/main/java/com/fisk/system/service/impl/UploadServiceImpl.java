package com.fisk.system.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.entity.DmpImagesPO;
import com.fisk.system.enums.PictureSuffixTypeEnum;
import com.fisk.system.mapper.DmpImagesMapper;
import com.fisk.system.service.UploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author WangYan
 * @date 2022/4/2 12:53
 */
@Service
public class UploadServiceImpl implements UploadService {

    @Resource
    private DmpImagesMapper dmpImagesMapper;
    @Value("${file.uploadurl}")
    private String uploadPath;

    @Override
    public String upload(MultipartFile file) {
        //如果文件夹不存在，创建
        File fileP = new File(uploadPath);

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

        try {
            file.transferTo(new File(fileP, fileName));
        } catch (IOException e) {
            throw new FkException(ResultEnum.ERROR);
        }

        // 图片完整路径
        String imagePath = "/file/dmp/images/" + fileName;

        // 保存到数据库
        DmpImagesPO chartImage = new DmpImagesPO();
        chartImage.setImagePath(imagePath);
        dmpImagesMapper.insert(chartImage);
        return imagePath;
    }
}
