package com.fisk.common.pdf.component;

import com.fisk.common.pdf.component.builder.HeaderFooterBuilder;
import com.fisk.common.pdf.component.builder.PDFBuilder;
import com.fisk.common.pdf.exception.PDFException;
import com.fisk.common.pdf.util.FreeMarkerUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;

@Slf4j
public class PDFKit {

    //PDF页眉、页脚定制工具
    private HeaderFooterBuilder headerFooterBuilder;
    private String saveFilePath;

    /**
     * @param templateName ftl模板名称
     * @param templatePath pdf模板路径
     * @param data         模板所需要的数据
     * @param footerValue  页尾名称
     * @description 导出pdf到文件
     */
    public String exportToFile(String templateName, String templatePath, String footerValue, Object data) {

        String htmlData = FreeMarkerUtil.getContent(templateName, templatePath, data);
        if (StringUtils.isEmpty(saveFilePath)) {
            saveFilePath = getDefaultSavePath(templateName);
        }
        File file = new File(saveFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream = null;
        try {
            //设置输出路径，创建PDF文件
            outputStream = new FileOutputStream(saveFilePath);
            //设置文档大小
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            //设置页眉页脚
            PDFBuilder builder = new PDFBuilder(headerFooterBuilder, templatePath, footerValue, data);
            builder.setPresentFontSize(10);
            writer.setPageEvent(builder);

            //输出为PDF文件
            convertToPDF(writer, document, htmlData, templatePath);
        } catch (Exception ex) {
            throw new PDFException("PDF export to File fail", ex);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return saveFilePath;

    }


    /**
     * 生成PDF到输出流中（ServletOutputStream用于下载PDF）
     *
     * @param templateName   ftl模板文件
     * @param templatePath   pdf模板路径
     * @param outputFileName 文件名称
     * @param footerValue    页脚名称
     * @param data           输入到FTL中的数据
     * @param response       HttpServletResponse
     * @return
     */
    public OutputStream exportToResponse(String templateName, String templatePath,
                                         String outputFileName, String footerValue,
                                         Object data, HttpServletResponse response) {

        String html = FreeMarkerUtil.getContent(templateName, templatePath, data);

        try {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + outputFileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");

            OutputStream out = null;
            ITextRenderer render = null;
            out = response.getOutputStream();
            //设置文档大小
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            //设置页眉页脚
            PDFBuilder builder = new PDFBuilder(headerFooterBuilder, templatePath, footerValue, data);
            writer.setPageEvent(builder);
            //输出为PDF文件
            convertToPDF(writer, document, html, templatePath);
            return out;
        } catch (Exception ex) {
            throw new PDFException("PDF export to response fail", ex);
        }

    }

    /**
     * @description PDF文件生成
     */
    private void convertToPDF(PdfWriter writer,
                              Document document,
                              String htmlString,
                              String templatePath) {
        //获取字体路径
        String fontPath = getFontPath(templatePath);
        document.open();
        try {
            XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                    new ByteArrayInputStream(htmlString.getBytes()),
                    XMLWorkerHelper.class.getResourceAsStream("/default.css"),
                    Charset.forName("UTF-8"), new XMLWorkerFontProvider(fontPath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new PDFException("PDF文件生成异常", e);
        } finally {
            document.close();
        }

    }

    /**
     * @description 创建默认保存路径
     */
    private String getDefaultSavePath(String fileName) {
        String classpath = PDFKit.class.getClassLoader().getResource("").getPath();
        String saveFilePath = classpath + "pdf/" + fileName;
        File f = new File(saveFilePath);
        if (!f.getParentFile().exists()) {
            f.mkdirs();
        }
        return saveFilePath;
    }

    /**
     * @description 获取字体设置路径
     */
    public static String getFontPath(String templatePath) {
//        String classpath = PDFKit.class.getClassLoader().getResource("").getPath();
//        String fontpath = classpath + "fonts";
        String fontpath = templatePath + File.separator + "fonts";//"/root/java/dataservice/pdf/fonts";
        return fontpath;
    }

    public HeaderFooterBuilder getHeaderFooterBuilder() {
        return headerFooterBuilder;
    }

    public void setHeaderFooterBuilder(HeaderFooterBuilder headerFooterBuilder) {
        this.headerFooterBuilder = headerFooterBuilder;
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }


}