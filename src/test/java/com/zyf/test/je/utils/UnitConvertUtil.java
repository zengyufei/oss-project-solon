package com.zyf.test.je.utils;

import com.zyf.test.je.annotations.JcBigDecConvert;
import com.zyf.test.je.enums.UnitConvertType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author : JCccc
 * @CreateTime : 2023/01/14
 * @Description :
 **/
@Slf4j
public class UnitConvertUtil {

    public static <T> void unitMapConvert(List<T> list, Map<String, UnitConvertType> propertyMap) {
        for (T t : list) {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (propertyMap.keySet().stream().anyMatch(x -> x.equals(declaredField.getName()))) {
                    try {
                        declaredField.setAccessible(true);
                        Object o = declaredField.get(t);
                        UnitConvertType unitConvertType = propertyMap.get(declaredField.getName());
                        if (o != null) {
                            if (unitConvertType.equals(UnitConvertType.PERCENTAGE)) {
                                BigDecimal bigDecimal = ((BigDecimal) o).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                declaredField.set(t, bigDecimal);
                            }
                            if (unitConvertType.equals(UnitConvertType.PERMIL)) {
                                BigDecimal bigDecimal = ((BigDecimal) o).multiply(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                declaredField.set(t, bigDecimal);
                            }
                            if (unitConvertType.equals(UnitConvertType.B)) {
                                BigDecimal bigDecimal = ((BigDecimal) o).divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                                declaredField.set(t, bigDecimal);
                            }
                            if (unitConvertType.equals(UnitConvertType.R)) {
                                BigDecimal bigDecimal = ((BigDecimal) o).setScale(2, BigDecimal.ROUND_HALF_UP);
                                declaredField.set(t, bigDecimal);
                            }
                        }
                    } catch (Exception ex) {
                        log.error("处理失败");
                        continue;
                    }

                }
            }
        }
    }

    public static <T> void unitAnnotateConvert(List<T> list) {
        for (T t : list) {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                try {
                    if (declaredField.getName().equals("serialVersionUID")) {
                        continue;
                    }
                    JcBigDecConvert myFieldAnn = declaredField.getAnnotation(JcBigDecConvert.class);
                    if (Objects.isNull(myFieldAnn)) {
                        continue;
                    }
                    UnitConvertType unitConvertType = myFieldAnn.name();
                    declaredField.setAccessible(true);
                    Object o = declaredField.get(t);
                    if (Objects.nonNull(o)) {
                        if (unitConvertType.equals(UnitConvertType.PERCENTAGE)) {
                            BigDecimal bigDecimal = ((BigDecimal) o).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            declaredField.set(t, bigDecimal);
                        }
                        if (unitConvertType.equals(UnitConvertType.PERMIL)) {
                            BigDecimal bigDecimal = ((BigDecimal) o).multiply(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            declaredField.set(t, bigDecimal);
                        }
                        if (unitConvertType.equals(UnitConvertType.B)) {
                            BigDecimal bigDecimal = ((BigDecimal) o).divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                            declaredField.set(t, bigDecimal);
                        }
                        if (unitConvertType.equals(UnitConvertType.R)) {
                            BigDecimal bigDecimal = ((BigDecimal) o).setScale(2, BigDecimal.ROUND_HALF_UP);
                            declaredField.set(t, bigDecimal);
                        }
                    }
                } catch (Exception ex) {
                    log.error("处理失败");
                }
            }
        }
    }
}
