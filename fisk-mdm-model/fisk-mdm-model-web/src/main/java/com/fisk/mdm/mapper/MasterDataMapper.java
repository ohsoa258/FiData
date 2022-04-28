package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 主数据映射器
 *
 * @author ChenYa
 * @date 2022/04/25
 */
@Mapper
public interface MasterDataMapper {
    /**
     * 根据code查询主数据
     *
     * @param tableName  表名
     * @param columnName 列名
     * @param code       查询条件
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    @Select("select #{columnName} from #{tableName} where code = #{code}")
    Map<String,Object> getByCode(@Param(value = "tableName")String tableName,
                                 @Param(value = "columnName")String columnName,
                                 @Param(value = "code")Integer code);


    Page<Map<String,Object>> getAll();
}
