package com.fisk.chartvisual;

import org.junit.Test;

import java.util.Optional;

public class CodeTest {

    @Test
    public void Test(){
        Integer a = null;
        Optional<Integer> b = Optional.ofNullable(a);
        System.out.println(b.orElse(0));
    }
}
