package com.fisk.dataaccess.utils.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class FileTxtUtils {

    public static void setFiles(String path, String data) {
        BufferedWriter out = null;
        try {
            //相对路径，如果没有则要建立一个新的output.txt文件
            File writeName = new File(path);
            if (!writeName.exists()) {
                //创建新文件,有同名的文件的话直接覆盖
                writeName.createNewFile();
            }
            FileWriter writer = new FileWriter(writeName);
            out = new BufferedWriter(writer);
            out.write(data);
            //把缓存区内容压入文件
            out.flush();
        } catch (IOException e) {
            log.error("setFiles ex:", e);
        } finally {
            closeBufferedWriter(out);
        }
    }

    public static void closeBufferedWriter(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("【closeBufferedWriter】关闭文本报错, ex", e);
            }
        }
    }

}
