package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataservice.entity.ApiConfigurePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;;
import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/7/8 14:58
 */
@Mapper
public interface ApiConfigureMapper extends BaseMapper<ApiConfigurePO> {

    /**
     * 查询指定表的数据
     * @param queryList
     * @param aggregationFieldList
     * @param groupList
     * @param tableName
     * @param whereList
     * @param currentPage
     * @param pageSize
     * @return
     */
    List<Map> queryData(@Param("queryList") List<String> queryList,
                        @Param("aggregationFieldList") List<String> aggregationFieldList,
                        @Param("groupList") List<String> groupList,
                        @Param("tableName") String tableName,
                        @Param("whereList") List<String> whereList,
                        @Param("currentPage")Integer currentPage,
                        @Param("pageSize")Integer pageSize
    );
}
