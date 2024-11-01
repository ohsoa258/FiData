package pd.tangqiao.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import pd.tangqiao.entity.TqLogStoragePO;
import pd.tangqiao.mapper.TqLogStoragePOMapper;
import pd.tangqiao.service.TqLogStoragePOService;

import java.util.Random;

/**
 * @author 56263
 * @description 针对表【tq_log_storage】的数据库操作Service实现
 * @createDate 2024-10-29 15:40:52
 */
@Service
public class TqLogStoragePOServiceImpl extends ServiceImpl<TqLogStoragePOMapper, TqLogStoragePO>
        implements TqLogStoragePOService {

    @Override
    public Object add(TqLogStoragePO po) {
        return this.save(po);
    }

    @Override
    public Object getLogList(Integer currentPage, Integer size) {
        return this.page(new Page<>(currentPage, size));
    }

    @Override
    public Object archiveCleanupBackups(Integer id, Integer option) {
        try {
            // 生成一个 2 到 4 秒之间的随机睡眠时间
            int minSleepTime = 2000; // 2 秒
            int maxSleepTime = 4000; // 4 秒
            Random random = new Random();
            int sleepTime = random.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}




