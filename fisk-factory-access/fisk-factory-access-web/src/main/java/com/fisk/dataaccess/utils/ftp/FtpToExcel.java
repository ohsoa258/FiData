package com.fisk.dataaccess.utils.ftp;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author : lock
 * @description: Excel parse util.
 * @date : 2021/12/27 10:45
 */
public class FtpToExcel {

    private static final String XLSX = ".xlsx";

    /**
     * 该方法判断excel版本
     *
     * @return org.apache.poi.ss.usermodel.Workbook
     * @description 该方法判断excel版本
     * @author Lock
     * @date 2022/4/7 11:21
     * @version v1.0
     * @params in 输入流
     * @params filename 文件名
     */
    static Workbook openWorkbook(InputStream in, String filename) throws IOException {
        Workbook wb = null;
        if (filename.endsWith(XLSX)) {
            // Excel 2007
            wb = new XSSFWorkbook(in);
        } else {
            // Excel 2003
            wb = (Workbook) new HSSFWorkbook(in);
        }
        return wb;
    }

    /**
     * 该方法处理excel的数据
     *
     * @return void
     * @description 该方法处理excel的数据
     * @author Lock
     * @date 2022/4/7 11:21
     * @version v1.0
     * @params fileName 文件名
     */
    public static void setExcelData(String fileName) throws Exception {
        // 创建输入流
        InputStream in = new FileInputStream(fileName);
        // 获取Excel文件对象
        Workbook wb = openWorkbook(in, fileName);
        // 获取文件的指定工作表m 默认的第一个Row row = null;
        Sheet sheet = wb.getSheetAt(0);
        // 总行数
        int totalRows = sheet.getLastRowNum();
        // 总列数，根据第一行得来的
        int totalCells = sheet.getRow(0).getLastCellNum();
        System.out.println("列数:" + totalCells + " 行数：" + totalRows);

        //依次获取每一行
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            // 获取行对象
            XSSFRow row = (XSSFRow) sheet.getRow(i);
            // 如果为空，不处理
            if (row == null) {
                continue;
            }

            if (row.getCell(i).getStringCellValue() != null && !"".equals(row.getCell(i).getStringCellValue())) {

                System.out.println(row.getCell(i).getStringCellValue());
            }

            //将第0列的标记为id，递增。遇到空的先不管，跳过
//            if (row.getCell(0) != null) {
//                Cell cellIndex = row.getCell(0);
////                System.out.print(cellIndex.getNumericCellValue());
//                System.out.println(cellIndex.getSheet());
//                System.out.println();
//                System.out.println("=============================================");
//                cellIndex.setCellValue(i);
//            } else {
//                XSSFCell cellIndex = row.createCell(0);
//                cellIndex.setCellValue(i);
//            }
        }
        //写入数据，关闭
//        OutputStream out = new FileOutputStream(fileName);
//        wb.write(out);
//        in.close();
//        out.close();
    }

    public static void main(String[] args) throws Exception {
//        String fileName="E:"+ File.separator+"hello.txt";
        String fileName = "E:\\学习文件\\tb_app_registration.xlsx";
        setExcelData(fileName);
//        File f=new File(fileName);
//        if(f.exists())
//            System.out.println("new file successfully");
//        Writer out =new FileWriter(f);
//        String str="hello";
//        out.write(str);
//        out.close();
    }
}