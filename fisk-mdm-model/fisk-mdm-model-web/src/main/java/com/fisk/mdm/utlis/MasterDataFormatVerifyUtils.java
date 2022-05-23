package com.fisk.mdm.utlis;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.masterdata.ImportDataVerifyDTO;
import com.fisk.mdm.enums.DataTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Component
@Slf4j
public class MasterDataFormatVerifyUtils {


    /**
     * 验证code
     *
     * @param data
     * @return
     */
    public static ImportDataVerifyDTO verifyCode(Map<String, Object> data) {
        try {
            ImportDataVerifyDTO dto = new ImportDataVerifyDTO();
            dto.setSuccess(true);
            if (StringUtils.isEmpty(data.get("code") == null ? "" : data.get("code").toString())
                    && !StringUtils.isEmpty(data.get("fidata_new_code") == null ? "" : data.get("fidata_new_code").toString())) {
                dto.setSuccess(false);
                dto.setErrorMsg("输入新编码时，编码列不能为空");
            }
            return dto;
        } catch (Exception e) {
            log.error("verifyCode:", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    /**
     * 获取Excel表格数据类型并校验
     *
     * @param cell
     * @return
     */
    public static ImportDataVerifyDTO getCellDataType(Cell cell, String columnDisplay, String dataType) {
        ImportDataVerifyDTO dto = new ImportDataVerifyDTO();
        dto.setSuccess(true);
        dto.setValue("");
        switch (cell.getCellType()) {
            //字符串
            case Cell.CELL_TYPE_STRING:
                dto.setValue(cell.getStringCellValue());
                break;
            //公式
            case Cell.CELL_TYPE_FORMULA:
                dto.setSuccess(false);
                dto.setValue(columnDisplay + "列存在公式,解析错误");
                break;
            //数字
            case Cell.CELL_TYPE_NUMERIC:
                //时间格式
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    String value = getFormatDate(cell.getDateCellValue(), dataType);
                    if (StringUtils.isEmpty(value)) {
                        dto.setSuccess(false);
                        dto.setErrorMsg(columnDisplay + "列存在错误时间格式");
                        break;
                    }
                    dto.setValue(value);
                } else {
                    //数字格式
                    if (DataTypeEnum.FLOAT.getName().equals(dataType)
                            || DataTypeEnum.MONEY.getName().equals(dataType)) {
                        dto.setValue(String.valueOf(cell.getNumericCellValue()));
                    } else {
                        DecimalFormat df = new DecimalFormat("#");
                        dto.setValue(df.format(cell.getNumericCellValue()));
                    }
                }
                break;
            //空白
            case Cell.CELL_TYPE_BLANK:
                dto.setValue("");
                break;
            //布尔值
            case Cell.CELL_TYPE_BOOLEAN:
                dto.setValue(String.valueOf(cell.getBooleanCellValue()));
                break;
            //错误值=CELL_TYPE_ERROR
            default:
                dto.setSuccess(false);
                dto.setErrorMsg(columnDisplay + "列存在不能解析的数据");
                break;

        }
        return dto;
    }

    /**
     * 时间格式化
     *
     * @param date
     * @return
     */
    public static String getFormatDate(Date date, String dataType) {
        if (DataTypeEnum.DATE.getName().equals(dataType)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else if (DataTypeEnum.TIMESTAMP.getName().equals(dataType)) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateTimeFormat.format(date);
        } else if (DataTypeEnum.TIME.getName().equals(dataType)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            return timeFormat.format(date);
        } else {
            return "";
        }
    }

}
