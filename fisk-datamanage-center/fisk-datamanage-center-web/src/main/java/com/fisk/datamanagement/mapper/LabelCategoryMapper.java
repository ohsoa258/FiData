package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryPathDto;
import com.fisk.datamanagement.entity.LabelCategoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface LabelCategoryMapper extends FKBaseMapper<LabelCategoryPO> {

    @Select("WITH RECURSIVE RecursiveCTE AS (\n" +
            "    SELECT id,category_parent_code, category_code, category_cn_name, category_cn_name AS Path, 1 AS Level\n" +
            "    FROM tb_category\n" +
            "\t\t where del_flag=1\n" +
            "\n" +
            "    UNION ALL\n" +
            "\n" +
            "    SELECT t.id,t.category_parent_code, t.category_code, t.category_cn_name, CONCAT(r.Path, ' > ', t.category_cn_name) , r.Level + 1\n" +
            "    FROM tb_category t\n" +
            "    INNER JOIN RecursiveCTE r ON t.category_parent_code = r.category_code\n" +
            "\t\tWHERE t.del_flag=1 )\n" +
            "SELECT id,category_parent_code, category_code, category_cn_name, Path\n" +
            "FROM (\n" +
            "    SELECT id,category_parent_code, category_code, category_cn_name, Path, ROW_NUMBER() OVER (PARTITION BY category_code ORDER BY Level DESC) AS RowNum\n" +
            "    FROM RecursiveCTE ) AS Subquery\n" +
            "WHERE RowNum = 1;")
    List<LabelCategoryPathDto> getLabelCategoryPath();
}
