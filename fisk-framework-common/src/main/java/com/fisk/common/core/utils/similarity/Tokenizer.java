package com.fisk.common.core.utils.similarity;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 中⽂分词⼯具类
 * @date 2022/4/18 14:14
 */
public class Tokenizer {
    public static List<Word> segment(String sentence) {
        //1、采⽤HanLP中⽂⾃然语⾔处理中标准分词进⾏分词
        List<Term> termList = HanLP.segment(sentence);
        //上⾯控制台打印信息就是这⾥输出的
        //System.out.println(termList.toString());
        //2、重新封装到Word对象中（term.word代表分词后的词语，term.nature代表改词的词性）
        return termList.stream().map(term -> new Word(term.word, term.nature.toString())).collect(Collectors.toList());
    }
}