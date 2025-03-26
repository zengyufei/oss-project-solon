package com.zyf.controller.apis;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.zyf.controller.entity.FileShowInfo;
import com.zyf.controller.entity.InputFileList;
import com.zyf.service.FileListService;
import com.zyf.service.caches.FileAllListCacheService;
import com.zyf.utils.CheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;
import java.util.Map;


/**
 * 文件列表控制器
 *
 * @author zyf
 * @date 2024/03/22
 */

@Slf4j
@Valid
@Mapping("/api")
@Controller
public class FileListController {

    @Inject
    private FileListService fileListService;
    @Inject
    private FileAllListCacheService fileAllListCacheService;

    /**
     * 列表
     *
     * @return {@link Result}<{@link List}<{@link Map}<{@link String}, {@link String}>>>
     * @throws Throwable 可抛出
     */
    @SaIgnore
    @Mapping("/file/list")
    public Result<List<FileShowInfo>> list(InputFileList inputFileList) throws Throwable {
        final Context ctx = Context.current();
        final boolean checked = CheckUtil.checkAuthOrRsa(ctx);
        if (!checked) {
            return Result.failure(Result.FAILURE_CODE, "请先登录");
        }
        inputFileList.setDirName(StrUtil.blankToDefault(inputFileList.getDirName(), "/"));
        final List<FileShowInfo> mapList = fileAllListCacheService.getValue(inputFileList, maps -> {
            final String orderByType = inputFileList.getOrderByType();
            final String orderByField = inputFileList.getOrderByField();
            maps.sort((o1, o2) -> {
                if (StrUtil.equalsIgnoreCase(orderByField, "1")) {
                    final Long left = o1.getSize();
                    final Long right = o2.getSize();
                    if (StrUtil.equalsIgnoreCase(orderByType, "1")) {
                        return right.compareTo(left);
                    } else {
                        return left.compareTo(right);
                    }
                } else {
                    final DateTime left = DateUtil.parse(o1.getLastModified());
                    final DateTime right = DateUtil.parse(o2.getLastModified());
                    if (StrUtil.equalsIgnoreCase(orderByType, "1")) {
                        // 日期倒序
                        return right.compareTo(left);
                    } else {
                        // 日期正序
                        return left.compareTo(right);
                    }
                }
            });
        });


        return Result.succeed(mapList);
    }


}
