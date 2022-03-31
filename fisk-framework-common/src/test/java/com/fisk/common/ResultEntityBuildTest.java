package com.fisk.common;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import org.junit.Test;

public class ResultEntityBuildTest {

    @Test
    public void buildTest(){
        ResultEntity<Object> res = ResultEntityBuild.build(ResultEnum.SUCCESS);
        System.out.println(res);
        assert res.code == ResultEnum.SUCCESS.getCode();
        assert res.data == null;
    }
}
