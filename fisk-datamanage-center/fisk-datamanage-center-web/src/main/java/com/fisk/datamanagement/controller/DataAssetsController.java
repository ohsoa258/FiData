package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IAssetsDirectory;
import com.fisk.datamanagement.service.IDataAssets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_ASSETS})
@RestController
@RequestMapping("/DataAssets")
public class DataAssetsController {

    @Resource
    IDataAssets service;
    @Resource
    IAssetsDirectory assetsDirectory;

    private static final int max_row = 65535;

    /**
     * 基于构造器注入
     */
    private final HttpServletResponse response;

    public DataAssetsController(HttpServletResponse response) {
        this.response = response;
    }

    @ApiOperation("获取表数据")
    @PostMapping("/getDataAssetsList")
    public ResultEntity<Object> getDataAssetsList(@Validated @RequestBody DataAssetsParameterDTO dto) {
        DataAssetsResultDTO result = service.getDataAssetsTableList(dto);
        if (dto.export) {
            exportTable(result, dto);
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

    @ApiOperation("资产目录列表")
    @GetMapping("/assetsDirectoryDataList")
    public ResultEntity<Object> assetsDirectoryDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, assetsDirectory.assetsDirectoryData());
    }

    /**
     * 导出Excel数据优化
     *
     * @param result
     * @param dto
     */
    public void exportTable(DataAssetsResultDTO result, DataAssetsParameterDTO dto) {

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        //从接口获取是否要显示名称；
        int columnNameIndex= dto.columnName.equalsIgnoreCase("displayname") ?1:0;

        Sheet sheet = workbook.createSheet(dto.tableName);
        SXSSFRow row1 = (SXSSFRow) sheet.createRow(0);
        for (int i = 0; i < result.columnList.size(); i++) {
            row1.createCell(i).setCellValue(result.columnList.get(i)[columnNameIndex]);
        }

        //记录额外创建的sheet数量
        Integer index = 0;
        for (int i = 0; i < result.dataArray.size(); i++) {
            if ((i + 1) % max_row == 0) {
                sheet = workbook.createSheet(dto.tableName + index);
                row1 = (SXSSFRow) sheet.createRow(0);
                for (int j = 0; j < result.columnList.size(); j++) {
                    row1.createCell(j).setCellValue(result.columnList.get(j)[columnNameIndex]);
                }
                index++;
            }
            row1 = (SXSSFRow) sheet.createRow((i + 1) - (index * max_row));
            JSONObject jsonObject = JSONObject.parseObject(result.dataArray.get(i).toString());
            for (int j = 0; j < result.columnList.size(); j++) {
                Object o = jsonObject.get(result.columnList.get(j)[0]);
                if (o == null) {
                    continue;
                }
                row1.createCell(j).setCellValue(o.toString());
            }

        }

        //将文件存到指定位置
        try {
            //输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-disposition", "attachment; filename=" + dto.tableName + ".xlsx");
            response.setContentType("application/x-xls");
            workbook.write(output);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
