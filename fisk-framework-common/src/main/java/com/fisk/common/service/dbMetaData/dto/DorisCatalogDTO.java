package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-09
 * @Description:
 */
@Data
public class DorisCatalogDTO {
    public String catalogName;
    public List<CataLogDatabase> cataLogDatabases;

    @Data
    public static class CataLogDatabase {
        public String databaseName;
        public List<CataLogTables> cataLogTables;
    }

    @Data
    public static class CataLogTables {
        public String tableName;
        public List<CataLogField> cataLogFields;
    }

    @Data
    public static class CataLogField {

        @ApiModelProperty(value = "字段名称")
        public String fieldName;



        @ApiModelProperty(value = "字段类型")
        public String type;



        @ApiModelProperty(value = "可否为空")
        public String ifNull;


        @ApiModelProperty(value = "是否为主键")
        public String Key;


        @ApiModelProperty(value = "字段的默认值")
        public String defaultValue;

        /**
         * 额外信息
         * 表示该字段的其他额外信息，例如自增（AUTO_INCREMENT）属性等。
         */
        @ApiModelProperty(value = "额外信息")
        public String extra;
    }
}
