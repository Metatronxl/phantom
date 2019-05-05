package com.noisy.utils;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-04-28 17:22
 * @Description:
 */
public class IPutils {


    /**
     * @Description: 拆分List<String>
     * @param size 拆分成的每个List的大小
     * @param target 待拆分List
     */
    public static List<List<String>> createList(List<String> target, int size) {

        List<List<String>> listArr = Lists.newArrayList();
        //获取被拆分的数组个数
        int arrSize = target.size() % size == 0 ? target.size() / size : target.size() / size + 1;
        for (int i = 0; i < arrSize; i++) {
            List<String> sub = Lists.newArrayList();
            //把指定索引数据放入到list中
            for (int j = i * size; j <= size * (i + 1) - 1; j++) {
                if (j <= target.size() - 1) {
                    sub.add(target.get(j));
                }
            }
            listArr.add(sub);
        }
        return listArr;
    }
}
