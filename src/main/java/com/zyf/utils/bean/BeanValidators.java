package com.zyf.utils.bean;

import org.noear.solon.validation.ValidatorManager;

/**
 * bean对象属性验证
 *
 * @author ruoyi
 */
public class BeanValidators {
    public static void validateWithException(Object object, Class<?>... groups) {
        ValidatorManager.validateOfEntity(object, groups);
    }
}
