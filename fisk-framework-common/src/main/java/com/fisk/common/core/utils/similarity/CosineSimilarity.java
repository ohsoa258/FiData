package com.fisk.common.core.utils.similarity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dick
 * @version 1.0
 * @description 相似率具体实现
 * @date 2022/4/18 14:17
 */

public class CosineSimilarity {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CosineSimilarity.class);

    /**
     * 1、计算两个字符串的相似度
     */
    public static double getSimilarity(String text1, String text2) {
        //如果为空，或者字符长度为0，则代表完全相同
        if (StringUtils.isBlank(text1) && StringUtils.isBlank(text2)) {
            return 1.0;
        }
        //如果⼀个为0或者空，⼀个不为，那说明完全不相似
        if (StringUtils.isBlank(text1) || StringUtils.isBlank(text2)) {
            return 0.0;
        }
        //如果两个字符串相等那返回1
        if (text1.equalsIgnoreCase(text2)) {
            return 1.0;
        }
        //第⼀步：进⾏分词
        List<Word> words1 = Tokenizer.segment(text1);
        List<Word> words2 = Tokenizer.segment(text2);
        return getSimilarity(words1, words2);
    }

    /**
     * 2、对于计算出的相似度保留⼩数点后六位
     */
    public static double getSimilarity(List<Word> words1, List<Word> words2) {
        double score = getSimilarityImpl(words1, words2);
        //(int) (score * 1000000 + 0.5)其实代表保留⼩数点后六位 ,因为1034234.213强制转换不就是1034234。对于强制转换添加0.5就等于四舍五⼊
        score = (int) (score * 1000000 + 0.5) / (double) 1000000;
        return score;
    }

    /**
     * ⽂本相似度计算判定⽅式：余弦相似度，通过计算两个向量的夹⾓余弦值来评估他们的相似度余弦夹⾓原理：向量a=(x1,y1),向量b=(x2,y2) similarity=a.b/|a|*|b| a.b=x1x2+y1y2
     * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     */
    public static double getSimilarityImpl(List<Word> words1, List<Word> words2) {
        // 向每⼀个Word对象的属性都注⼊weight（权重）属性值
        taggingWeightByFrequency(words1, words2);
        //第⼆步：计算词频
        //通过上⼀步让每个Word对象都有权重值，那么在封装到map中（key是词，value是该词出现的次数（即权重））
        Map<String, Float> weightMap1 = getFastSearchMap(words1);
        Map<String, Float> weightMap2 = getFastSearchMap(words2);
        //将所有词都装⼊set容器中
        Set<Word> words = new HashSet<>();
        words.addAll(words1);
        words.addAll(words2);
        AtomicFloat ab = new AtomicFloat();// a.b
        AtomicFloat aa = new AtomicFloat();// |a|的平⽅
        AtomicFloat bb = new AtomicFloat();// |b|的平⽅
        // 第三步：写出词频向量，后进⾏计算
        words.parallelStream().forEach(word -> {
            //看同⼀词在a、b两个集合出现的此次
            Float x1 = weightMap1.get(word.getName());
            Float x2 = weightMap2.get(word.getName());
            if (x1 != null && x2 != null) {
                //x1x2
                float oneOfTheDimension = x1 * x2;

                ab.addAndGet(oneOfTheDimension);
            }
            if (x1 != null) {
                //(x1)^2
                float oneOfTheDimension = x1 * x1;
                //+
                aa.addAndGet(oneOfTheDimension);
            }
            if (x2 != null) {
                //(x2)^2
                float oneOfTheDimension = x2 * x2;
                //+
                bb.addAndGet(oneOfTheDimension);
            }
        });
        //|a| 对aa开⽅
        double aaa = Math.sqrt(aa.doubleValue());
        //|b| 对bb开⽅
        double bbb = Math.sqrt(bb.doubleValue());
        //使⽤BigDecimal保证精确计算浮点数
        //double aabb = aaa * bbb;
        BigDecimal aabb = BigDecimal.valueOf(aaa).multiply(BigDecimal.valueOf(bbb));
        //similarity=a.b/|a|*|b|
        //divide参数说明：aabb被除数,9表⽰⼩数点后保留9位，最后⼀个表⽰⽤标准的四舍五⼊法
        double cos = BigDecimal.valueOf(ab.get()).divide(aabb, 9, BigDecimal.ROUND_HALF_UP).doubleValue();
        return cos;
    }

    /**
     * 向每⼀个Word对象的属性都注⼊weight（权重）属性值
     */
    protected static void taggingWeightByFrequency(List<Word> words1, List<Word> words2) {
        if (words1.get(0).getWeight() != null && words2.get(0).getWeight() != null) {
            return;
        }
        //词频统计（key是词，value是该词在这段句⼦中出现的次数）
        Map<String, AtomicInteger> frequency1 = getFrequency(words1);
        Map<String, AtomicInteger> frequency2 = getFrequency(words2);
        //如果是DEBUG模式输出词频统计信息
        //        if (LOGGER.isDebugEnabled()) {
        //            LOGGER.debug("词频统计1：\n{}", getWordsFrequencyString(frequency1));
        //            LOGGER.debug("词频统计2：\n{}", getWordsFrequencyString(frequency2));
        //        }
        // 标注权重（该词出现的次数）
        words1.parallelStream().forEach(word -> word.setWeight(frequency1.get(word.getName()).floatValue()));
        words2.parallelStream().forEach(word -> word.setWeight(frequency2.get(word.getName()).floatValue()));
    }

    /**
     * 统计词频
     *
     * @return 词频统计图
     */
    private static Map<String, AtomicInteger> getFrequency(List<Word> words) {
        Map<String, AtomicInteger> freq = new HashMap<>();
        words.forEach(i -> freq.computeIfAbsent(i.getName(), k -> new AtomicInteger()).incrementAndGet());
        return freq;
    }

    /**
     * 输出：词频统计信息
     */
    private static String getWordsFrequencyString(Map<String, AtomicInteger> frequency) {
        StringBuilder str = new StringBuilder();
        if (frequency != null && !frequency.isEmpty()) {
            AtomicInteger integer = new AtomicInteger();
            frequency.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).forEach(
                    i -> str.append("\t").append(integer.incrementAndGet()).append("、").append(i.getKey()).append("=")
                            .append(i.getValue()).append("\n"));
        }
        str.setLength(str.length() - 1);
        return str.toString();
    }

    /**
     * 构造权重快速搜索容器
     */
    protected static Map<String, Float> getFastSearchMap(List<Word> words) {
        if (CollectionUtils.isEmpty(words)) {
            return Collections.emptyMap();
        }
        Map<String, Float> weightMap = new ConcurrentHashMap<>(words.size());
        words.parallelStream().forEach(i -> {
            if (i.getWeight() != null) {
                weightMap.put(i.getName(), i.getWeight());
            } else {
                LOGGER.error("no word weight info:" + i.getName());
            }
        });
        return weightMap;
    }
}
