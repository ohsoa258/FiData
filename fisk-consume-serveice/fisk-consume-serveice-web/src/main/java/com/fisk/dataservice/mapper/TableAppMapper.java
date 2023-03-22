package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.tableservice.TableAppPageDTO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TableAppMapper extends FKBaseMapper<TableAppPO> {
    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<TableAppVO> filter(Page<TableAppVO> page, @Param("query") TableAppPageDTO query);

    /**
     * 新增一条api数据
     *
     * @return 执行结果
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("INSERT INTO `tb_table_app`(`app_name`, `app_desc`, `app_principal`, `app_principal_email`, `create_time`, `create_user`,`del_flag`) VALUES (#{appName}, #{appDesc}, #{appPrincipal}, #{appPrincipalEmail},#{createTime}, #{createUser},1);")
    int insertOne(TableAppPO po);
}