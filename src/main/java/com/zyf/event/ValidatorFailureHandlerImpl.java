package com.zyf.event;

import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.ValidatorFailureHandler;

import java.lang.annotation.Annotation;

// 通过定义 ValidatorFailureHandler 实现类的组件，实现自动注册。
@Component
public class ValidatorFailureHandlerImpl implements ValidatorFailureHandler {
    @Override
    public boolean onFailure(Context ctx, Annotation anno, Result rst, String message) throws Throwable {
        ctx.setHandled(true); // 表示后面不再处理
        ctx.setRendered(true); // 表示后面不再渲染

        if (Utils.isEmpty(message)) {
            message = new StringBuilder(100)
                    .append("@")
                    .append(anno.annotationType().getSimpleName())
                    .append(" verification failed")
                    .toString();
        }

        ctx.output(message);

        return true;
    }
}
