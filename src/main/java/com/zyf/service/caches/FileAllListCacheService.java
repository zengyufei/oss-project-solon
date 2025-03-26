package com.zyf.service.caches;

import com.zyf.controller.entity.FileShowInfo;
import com.zyf.controller.entity.InputFileList;
import com.zyf.service.FileListService;
import com.zyf.utils.BaseGuavaCache;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FileAllListCacheService extends BaseGuavaCache<InputFileList, List<FileShowInfo>> {

    @Inject("${files.cache.expire}")
    private int expire;
    @Inject("${files.cache.refresh}")
    private int refresh;

    @Inject
    private FileListService fileListService;

    @Override
    public void init() {
        this.setExpire(expire, TimeUnit.MILLISECONDS);
        this.setRefresh(refresh, TimeUnit.MILLISECONDS);
    }

    @Override
    public void loadValueWhenStarted() {
    }

    @Override
    protected List<FileShowInfo> getValueWhenExpired(InputFileList key) throws Exception {
        return fileListService.getListResult(key);
    }

    @Override
    protected void getValueBefore(InputFileList key) throws Exception {

        key.setAll(true);
    }
}
