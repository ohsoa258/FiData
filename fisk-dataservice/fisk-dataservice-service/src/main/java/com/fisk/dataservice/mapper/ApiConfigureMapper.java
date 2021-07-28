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
     * @param splitSql 拼接sql
     * @param currentPage  当前页数
     * @param pageSize     页数大小
     * @return
     */
    List<Map> queryData(@Param("splitSql") String splitSql,
                        @Param("currentPage")Integer currentPage,
                        @Param("pageSize")Integer pageSize
    );
}
