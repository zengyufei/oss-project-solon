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
public class PkDiff<T> {

    private List<T> addList;
    private List<T> delList;
    // 已存在的对象集合 对应的新值
    private Map<T, T> updateMap;

    public List<T> getEffectList() {
        final List<T> list = new ArrayList<>(addList);
        list.addAll(getExistsList());
        return list;
    }

    public PkDiff<T> addConsumer(BiConsumer<PkDiff<T>, List<T>> biConsumer) {
        if (CollUtil.isNotEmpty(addList)) {
            biConsumer.accept(this, addList);
        }
        return this;
    }

    public PkDiff<T> delConsumer(BiConsumer<PkDiff<T>, List<T>> biConsumer) {
        if (CollUtil.isNotEmpty(delList)) {
            biConsumer.accept(this, delList);
        }
        return this;
    }

    public PkDiff<T> existsConsumer(BiConsumer<PkDiff<T>, List<T>> biConsumer) {
        final List<T> existsList = getExistsList();
        if (CollUtil.isNotEmpty(existsList)) {
            biConsumer.accept(this, existsList);
        }
        return this;
    }


    public PkDiff<T> updateConsumer(BiConsumer<PkDiff<T>, Map<T, T>> biConsumer) {
        if (MapUtil.isNotEmpty(updateMap)) {
            biConsumer.accept(this, updateMap);
        }
        return this;
    }

    public PkDiff<T> forEachUpdateMapConsumer(BiConsumer<T, T> biConsumer) {
        if (MapUtil.isNotEmpty(updateMap)) {
            for (Map.Entry<T, T> entry : updateMap.entrySet()) {
                final T oldPo = entry.getKey();
                final T newPo = entry.getValue();
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

    public List<T> getUpdateMapValues(){
        return updateMap.values().stream().toList();
    }
}
