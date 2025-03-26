package com.zyf.utils.lambda;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Data
@AllArgsConstructor
public class PkDiff2<T, R> {

    private List<R> addList;
    private List<T> delList;
    // 已存在的对象集合 对应的新值
    private Map<T, R> updateMap;

   public List<Object> getEffectList() {
        final List<Object> list = new ArrayList<>(addList);
        list.addAll(getExistsList());
        return list;
    }

    public PkDiff2<T, R> addConsumer(BiConsumer<PkDiff2<T, R>, List<R>> biConsumer) {
        if (CollUtil.isNotEmpty(addList)) {
            biConsumer.accept(this, addList);
        }
        return this;
    }

    public PkDiff2<T, R> delConsumer(BiConsumer<PkDiff2<T, R>, List<T>> biConsumer) {
        if (CollUtil.isNotEmpty(delList)) {
            biConsumer.accept(this, delList);
        }
        return this;
    }

    public PkDiff2<T, R> existsConsumer(BiConsumer<PkDiff2<T, R>, List<T>> biConsumer) {
        final List<T> existsList = getExistsList();
        if (CollUtil.isNotEmpty(existsList)) {
            biConsumer.accept(this, existsList);
        }
        return this;
    }


    public PkDiff2<T, R> updateConsumer(BiConsumer<PkDiff2<T, R>, Map<T, R>> biConsumer) {
        if (MapUtil.isNotEmpty(updateMap)) {
            biConsumer.accept(this, updateMap);
        }
        return this;
    }

    public PkDiff2<T, R> forEachUpdateMapConsumer(BiConsumer<T, R> biConsumer) {
        if (MapUtil.isNotEmpty(updateMap)) {
            for (Map.Entry<T, R> entry : updateMap.entrySet()) {
                final T oldPo = entry.getKey();
                final R newPo = entry.getValue();
                biConsumer.accept(oldPo, newPo);
            }
        }
        return this;
    }

    public List<T> getExistsList(){
        return getUpdateMapKeys();
    }

    public List<T> getUpdateMapKeys(){
        return updateMap.keySet().stream().toList();
    }

    public List<R> getUpdateMapValues(){
        return updateMap.values().stream().toList();
    }
}
