package com.fisk.common.core.utils.office.pdf.util;

import com.fisk.common.core.utils.office.pdf.exception.FreeMarkerException;
import com.fisk.common.core.utils.office.pdf.exception.PDFException;
import com.google.common.collect.Maps;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * FREEMARKER 模板工具类
 */
public class FreeMarkerUtil {

    private static final String WINDOWS_SPLIT = "\\";

    private static final String UTF_8 = "UTF-8";

    private static Map<String, FileTemplateLoader> fileTemplateLoaderCache = Maps.newConcurrentMap();

    private static Map<String, Configuration> configurationCache = Maps.newConcurrentMap();

    public static Configuration getConfiguration(String templateFilePath) {
        try {
            if (null != configurationCache.get(templateFilePath)) {
                return configurationCache.get(templateFilePath);
            }
            Configuration config = new Configuration(Configuration.VERSION_2_3_25);
            config.setDefaultEncoding(UTF_8);
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            config.setLogTemplateExceptions(false);
            FileTemplateLoader fileTemplateLoader = null;
            if (null != fileTemplateLoaderCache.get(templateFilePath)) {
                fileTemplateLoader = fileTemplateLoaderCache.get(templateFilePath);
            }
            fileTemplateLoader = new FileTemplateLoader(new File(templateFilePath));
            fileTemplateLoaderCache.put(templateFilePath, fileTemplateLoader);
            config.setTemplateLoader(fileTemplateLoader);
            configurationCache.put(templateFilePath, config);
            return config;
        } catch (IOException e) {
            throw new FreeMarkerException("fileTemplateLoader init error!", e);
        }
    }


    /**
     * @param templateName 模板名称
     * @param templatePath pdf模板路径
     * @param data         数据源
     * @description 获取模板
     */
    public static String getContent(String templateName, String templatePath, Object data) {
        File file = getPDFTemplatePath(templateName, templatePath);
        String templateFileName = file.getName();
        String templateFilePath = file.getPath().replace(templateFileName, "");
//        if (StringUtils.isEmpty(templatePath)) {
//            throw new FreeMarkerException("templatePath can not be empty!");
//        }
        try {
            Template template = getConfiguration(templateFilePath).getTemplate(templateFileName);
            StringWriter writer = new StringWriter();
            // 如果data中某个对象的属性值为null，则会触发异常，解决方案为：模板中使用此属性时加上感叹号，例如：api.name!
            template.process(data, writer);
            writer.flush();
            return writer.toString();
        } catch (Exception ex) {
            throw new FreeMarkerException("FreeMarkerUtil process fail", ex);
        }
    }


    private static String getTemplatePath(String templatePath) {
        if (StringUtils.isEmpty(templatePath)) {
            return "";
        }
        if (templatePath.contains(WINDOWS_SPLIT)) {
            return templatePath.substring(0, templatePath.lastIndexOf(WINDOWS_SPLIT));
        }
        return templatePath.substring(0, templatePath.lastIndexOf("/"));
    }

    private static String getTemplateName(String templatePath) {
        if (StringUtils.isEmpty(templatePath)) {
            return "";
        }
        if (templatePath.contains(WINDOWS_SPLIT)) {
            return templatePath.substring(templatePath.lastIndexOf(WINDOWS_SPLIT) + 1);
        }
        return templatePath.substring(templatePath.lastIndexOf("/") + 1);
    }

    /**
     * @param templateName PDF模板文件名
     * @param templatePath pdf模板路径
     * @return 匹配到的模板名
     * @description 获取PDF的模板路径,
     * 默认按照PDF文件名匹对应模板
     */
    public static File getPDFTemplatePath(String templateName,String templatePath) {
//        String classpath = PDFKit.class.getClassLoader().getResource("").getPath();
//        String templatePath = classpath + File.separator + "templates" + File.separator + fileName;
//        File file = new File(templatePath);
//        if (!file.exists()) {
//            throw new PDFException("PDF模板文件不存在,请检查templates文件夹!");
//        }
        //Resource resource = new ClassPathResource("templates!" + File.separator + fileName);
        String filePath = templatePath + File.separator + "templates" + File.separator + templateName;
        File file = new File(filePath);

        //file = resource.getFile();
        if (!file.exists()) {
            throw new PDFException("PDF模板文件不存在,请检查templates文件夹!");
        }


//        String pdfFileName = fileName.substring(0, fileName.lastIndexOf("."));
//        File defaultTemplate = null;
//        File matchTemplate = file;
//        for (File f : file.listFiles()) {
//            if (!f.isFile()) {
//                continue;
//            }
//            String templateName = f.getName();
//            if (templateName.lastIndexOf(".ftl") == -1) {
//                continue;
//            }
//            if (defaultTemplate == null) {
//                defaultTemplate = f;
//            }
//            if (StringUtils.isEmpty(fileName) && defaultTemplate != null) {
//                break;
//            }
//            templateName = templateName.substring(0, templateName.lastIndexOf("."));
//            if (templateName.toLowerCase().equals(pdfFileName.toLowerCase())) {
//                matchTemplate = f;
//                break;
//            }
//        }
//        if (matchTemplate != null) {
//            return matchTemplate.getAbsolutePath();
//        }
//        if (defaultTemplate != null) {
//            return defaultTemplate.getAbsolutePath();
//        }

        return file;
    }


}
