package com.fisk.common.core.utils.similarity;

import lombok.Data;

import java.util.Objects;

/**
 * @author dick
 * @version 1.0
 * @description 封装分词结果
 * @date 2022/4/18 14:16
 */
@Data
public class Word implements Comparable {
    // 词名
    private String name;
    // 词性
    private String pos;
    // 权重，⽤于词向量分析
    private Float weight;

    public Word(String name, String pos) {
        this.name = name;
        this.pos = pos;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if(name != null) {
            str.append(name);
        }
        if (pos != null) {
            str.append("/").append(pos);
        }
        return str.toString();
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (this.name == null) {
            return -1;
        }
        if (o == null) {
            return 1;
        }
        if (!(o instanceof Word)) {
            return 1;
        }
        String t = ((Word) o).getName();
        if (t == null) {
            return 1;
        }
        return this.name.compareTo(t);
    }
}

