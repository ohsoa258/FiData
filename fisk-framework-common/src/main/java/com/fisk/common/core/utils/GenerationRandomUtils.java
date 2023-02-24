package com.fisk.common.core.utils;

import java.util.Random;

/**
 * @author JianWenYang
 */
public class GenerationRandomUtils {

    public static int generateRandom6DigitNumber() {
        Random random = new Random();
        return random.nextInt(900000) + 100000;
    }

}
