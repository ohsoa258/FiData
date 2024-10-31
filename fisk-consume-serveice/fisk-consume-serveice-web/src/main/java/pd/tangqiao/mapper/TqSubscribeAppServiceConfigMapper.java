package pd.tangqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pd.tangqiao.entity.TqSubscribeApiConfigVO;
import pd.tangqiao.entity.TqSubscribeAppServiceConfigPO;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-10-30 15:38:27
 */
@Mapper
public interface TqSubscribeAppServiceConfigMapper extends BaseMapper<TqSubscribeAppServiceConfigPO> {

    List<TqSubscribeApiConfigVO> apilist(@Param("appIds") List<Integer> appIds);
}
