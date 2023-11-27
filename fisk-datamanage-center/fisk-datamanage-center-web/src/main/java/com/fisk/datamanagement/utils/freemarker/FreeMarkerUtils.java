package com.fisk.datamanagement.utils.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-11-23
 * @Description:
 */
public class FreeMarkerUtils {
    /**
     * 使用 Freemarker 生成 Word 文件
     * @param templateName 模板文件路径名称
     * @param fileName 生成的文件路径以及名称
     * @param dataModel 填充的数据对象
     */
    public static void exportWord(String templateName, String fileName, Map<String, Object> dataModel, HttpServletResponse response) {
        generateFile(templateName, fileName, dataModel, response);
    }

    /**
     * 使用 Freemarker 生成指定文件
     * @param templateName 模板文件路径名称
     * @param fileName 生成的文件路径以及名称
     * @param dataModel 填充的数据对象
     */
    private static void generateFile(String templateName, String fileName, Map<String, Object> dataModel, HttpServletResponse response) {
        try {
            // 1、创建配置对象
            Configuration config = new Configuration(Configuration.VERSION_2_3_29);
            config.setDefaultEncoding("utf-8");
            config.setClassForTemplateLoading(FreeMarkerUtils.class, "/templates");
            // 2、获取模板文件
            Template template = config.getTemplate(templateName);

            // 3、将生成的文件写入到字节数组输出流中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
            template.process(dataModel, writer);
            writer.flush();
            // 清空response
            response.reset();
            // 4、设置HTTP响应头信息
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            // 5、将字节数组输出流中的内容写入到HTTP响应输出流中
            OutputStream out = response.getOutputStream();
            baos.writeTo(out);
            out.flush();

            // 6、关闭流
            writer.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
