package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.businessclassification.BusinessClassificationDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryCategoryPathDto;
import com.fisk.datamanagement.dto.glossary.GlossaryLibraryDTO;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface GlossaryLibraryMapper extends FKBaseMapper<GlossaryLibraryPO> {
    @Select("WITH RECURSIVE RecursiveCTE AS (\n" +
            "    SELECT pid, id, name, name AS Path, 1 AS Level\n" +
            "    FROM tb_glossary_library\n" +
            "\n" +
            "\n" +
            "    UNION ALL\n" +
            "\n" +
            "    SELECT t.pid, t.id, t.name, CONCAT(r.Path, ' > ', t.name), r.Level + 1\n" +
            "    FROM tb_glossary_library t\n" +
            "    INNER JOIN RecursiveCTE r ON t.pid = r.id\n" +
            ")\n" +
            "SELECT pid, id,name, Path\n" +
            "FROM (\n" +
            "    SELECT pid, id,name, Path, ROW_NUMBER() OVER (PARTITION BY id ORDER BY Level DESC) AS RowNum\n" +
            "    FROM RecursiveCTE\n" +
            ") AS Subquery\n" +
            "WHERE RowNum = 1;\n")
    List<GlossaryCategoryPathDto> getGlossaryCategoryPath();
}
