package com.zyf.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.zyf.AppConstant;
import lombok.RequiredArgsConstructor;
import org.noear.solon.annotation.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义Sa-Token权限认证接口扩展
 *
 * @author kong
 */
@Component
@RequiredArgsConstructor
public class UserAuthService implements StpInterface {


    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 1. 声明权限码集合
        List<String> permissionList = new ArrayList<>();
        if (loginType.equals(StpUtil.TYPE)) {
            // 2. 遍历角色列表，查询拥有的权限码
            SaSession session = StpUtil.getTokenSession();
            List<String> list = session.get(AppConstant.PERMISSION_LIST, () -> {
                final List<String> authCodes = new ArrayList<>();
                final List<String> roleList = getRoleList(loginId, loginType);
                for (String role : roleList) {
                    if (AppConstant.ADMIN.equals(role)) {
                        authCodes.add("upload");
                        authCodes.add("download");
                        authCodes.add("delete");
                        authCodes.add("query");
                    }
                    if (AppConstant.USER.equals(role)) {
                        authCodes.add("upload");
                        authCodes.add("download");
                        authCodes.add("delete");
                        authCodes.add("query");
                    }
                    if (AppConstant.YOUKE.equals(role)) {
                        authCodes.add("download");
                        authCodes.add("query");
                    }
                }
                return authCodes;
            });
            permissionList.addAll(list);
            // 3. 返回权限码集合
            return permissionList;
        }
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginType.equals(StpUtil.TYPE)) {
            SaSession session = StpUtil.getTokenSession();
            return session.get(AppConstant.ROLE_LIST, () -> {
                final List<String> list = new ArrayList<>();
                list.add(AppConstant.ADMIN);
                list.add(AppConstant.USER);
                list.add(AppConstant.YOUKE);
                return list;
            });
        }
        return Collections.emptyList();
    }

}
