package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pd.tangqiao.entity.TqDatacheckReportQueryDTO;
import pd.tangqiao.entity.TqSubscribeAppConfigPO;
import pd.tangqiao.entity.TqSubscribeAppConfigVO;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-29 15:00:58
 */
@Mapper
public interface TqSubscribeAppConfigMapper extends BaseMapper<TqSubscribeAppConfigPO> {

    Page<TqSubscribeAppConfigVO> getAll(Page<TqSubscribeAppConfigVO> page);
}
