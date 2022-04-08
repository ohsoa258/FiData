package com.fisk.chartvisual.util.dbhelper.zip;

import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author WangYan
 * @date 2022/2/10 10:23
 */
@Slf4j
public class ZipHelper {

    public static final String SUFFIX = ".zip";

    /**
     * 删除临时zipw文件
     *
     * @param filePath
     * @return
     */
    public static void deleteFile(String filePath) {
        File dir = new File(filePath);
        if (dir.getName().endsWith(SUFFIX)) {
            if (dir.delete()) {
                log.info("zip文件已经删除");
            } else {
                log.info("zip文件删除失败");
            }
        }
    }

    /**
     * 判断是否zip
     * @param file
     * @return
     */
    public static boolean isZip(MultipartFile file){
        if (!file.isEmpty()){
            if(file.getOriginalFilename().endsWith(SUFFIX)){
                return true;
            }else{
                throw new FkException(ResultEnum.VISUAL_FOLDER_ERROR);
            }
        }else {
            throw new FkException(ResultEnum.VISUAL_FOLDER_ERROR);
        }
    }
}
