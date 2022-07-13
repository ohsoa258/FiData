package com.fisk.mdm.utlis;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author WangYan
 * @Date 2022/7/12 16:27
 * @Version 1.0
 */
@Component
public class FileConvertUtils {

    /**
     * 文件流转换
     * @param filePath
     * @return
     */
    public static FileItem createFileItem(String filePath) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "textField";
        int num = filePath.lastIndexOf(".");
        String extFile = filePath.substring(num);
        String path = filePath.substring(0, num);
        path = path.replace("\\", "/");
        String[] fileNames = path.split("/");
        String fileName = fileNames[fileNames.length - 1];
        FileItem item = factory.createItem(textFieldName, "text/plain", true, fileName + extFile);
        File newfile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(newfile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

}
