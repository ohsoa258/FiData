package com.fisk.system.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author WangYan
 * @date 2022/4/2 12:53
 */
public interface UploadService {

    /**
     * 将图片上传到服务器
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
