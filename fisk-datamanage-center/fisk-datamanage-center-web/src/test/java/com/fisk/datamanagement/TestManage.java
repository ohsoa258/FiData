package com.fisk.datamanagement;

import com.fisk.datamanagement.service.impl.MetaAnalysisEmailConfigServiceImpl;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestManage {

    @Resource
    private MetaAnalysisEmailConfigServiceImpl metaAnalysisEmailConfigService;

    @org.junit.Test
    public void testSendEmail(){
        metaAnalysisEmailConfigService.sendEmailOfMetaAudit();
    }

}
