package com.fisk.common.service.sqlparser;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/12/7 10:12
 */
public class SqlParserFactory {

    public static ISqlParser parser(ParserVersion version) throws Exception {
        switch (version) {
            case V1:
                return new SqlParserV1();
            default:
                throw new Exception("未知的版本类型");
        }
    }
}
