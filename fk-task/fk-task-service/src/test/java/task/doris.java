package task;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.mapper.TBETLLogMapper;
import org.junit.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 18:04
 * Description:
 */
@MapperScan("com.fisk.task.mapper")
@DS("datainputdb")
public class doris {

    @Autowired
    private TBETLLogMapper logMapper;

    @Test
   public void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<TBETLlogPO> loglist = logMapper.selectList(null);
        //Assert.assertEquals(5, userList.size());
        loglist.forEach(System.out::println);
    }

//    public static void main(String[] args) {
//        UpdateLogAndImportDataDTO inpData = JSON.parseObject("{\"code\":\"a25396f3-f112-11eb-bfeb-0242ac110005\"}", UpdateLogAndImportDataDTO.class);
//        //etlmapper.update(inpData)
//        TBETLlogPO modeletllog = inpData.toEntity(TBETLlogPO.class);
//        modeletllog.setStatus(2);
//        UpdateWrapper<TBETLlogPO> updateWrapper=new UpdateWrapper<>();
//        updateWrapper.eq("code",inpData.code);
//        int updateres=etlmapper.update(modeletllog,updateWrapper);
//    }
}
