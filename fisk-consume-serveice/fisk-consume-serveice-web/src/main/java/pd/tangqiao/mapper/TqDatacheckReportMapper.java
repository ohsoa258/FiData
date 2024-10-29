package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pd.tangqiao.entity.TqDatacheckReportPO;
import pd.tangqiao.entity.TqDatacheckReportQueryDTO;
import pd.tangqiao.entity.TqDatacheckReportVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 10:56:49
 */
@Mapper
public interface TqDatacheckReportMapper extends BaseMapper<TqDatacheckReportPO> {

    Page<TqDatacheckReportVO> getAll(Page<TqDatacheckReportVO> page, @Param("query")TqDatacheckReportQueryDTO query);
}
