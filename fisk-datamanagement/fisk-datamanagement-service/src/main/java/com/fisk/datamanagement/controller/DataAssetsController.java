package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;
import com.fisk.datamanagement.service.IDataAssets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_ASSETS})
@RestController
@RequestMapping("/DataAssets")
public class DataAssetsController {

    @Resource
    IDataAssets service;

    @ApiOperation("获取表数据")
    @PostMapping("/getDataAssetsList")
    public ResultEntity<Object> getDataAssetsList(@Validated @RequestBody DataAssetsParameterDTO dto)throws IOException {
        DataAssetsResultDTO result = service.getDataAssetsTableList(dto);
        if (dto.export)
        {
            exportTable(result,dto.tableName);
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS,result);
    }

    /**
     *
     * @param result
     * @param tableName
     * @throws IOException
     */
    public void exportTable( DataAssetsResultDTO result,String tableName) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet1");
        HSSFRow row1 = sheet.createRow(0);
        for (int i = 0; i < result.columnList.size(); i++) {
            row1.createCell(i).setCellValue(result.columnList.get(i));
        }
        for (int i=0;i<result.dataArray.size();i++)
        {
            HSSFRow row = sheet.createRow(i+1);
            JSONObject jsonObject = JSONObject.parseObject(result.dataArray.get(i).toString());
            for (int j = 0; j < result.columnList.size(); j++)
            {
                row.createCell(j).setCellValue(jsonObject.get(result.columnList.get(j)).toString());
            }
        }
        //将文件存到指定位置
        try {
            FileOutputStream file = new FileOutputStream("C:\\"+tableName+".xls");
            workbook.write(file);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
