package net.aooms.rbac.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.kisso.SSOHelper;
import com.baomidou.kisso.security.token.SSOToken;
import net.aooms.core.Aooms;
import net.aooms.core.AoomsConstants;
import net.aooms.core.authentication.AuthenticationInfo;
import net.aooms.core.module.mybatis.Db;
import net.aooms.core.module.mybatis.SqlPara;
import net.aooms.core.record.Record;
import net.aooms.core.service.GenericService;
import net.aooms.core.util.PasswordHash;
import net.aooms.core.web.AoomsContext;
import net.aooms.rbac.mapper.RbacMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户登录
 * Created by 风象南(yuboon) on 2018-11-01
 */
@Service
public class LoginService extends GenericService {

    @Autowired
    private Db db;

    @Transactional(readOnly = true)
    public void validateAccount() {
        Record record = db.findObject(RbacMapper.PKG.by("UserMapper.findByAccount"),SqlPara.fromDataBoss());
        if(record == null){
            getResult().failure(HttpStatus.HTTP_UNAUTHORIZED,"用户名或密码错误");
            return;
        }

        String storePassword = record.getString("password");
        if(!PasswordHash.validatePassword(getParaString("password"),storePassword)){
            getResult().failure(HttpStatus.HTTP_UNAUTHORIZED,"用户名或密码错误");
            return;
        }


        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        //BeanUtil.fillBeanWithMap(record, authenticationInfo, true, true);
        authenticationInfo.setId(record.getString("id"));
        authenticationInfo.setUserName(record.getString("user_name"));
        authenticationInfo.setUserNickname(record.getString("user_nickname"));
        authenticationInfo.setAccount(record.getString("account"));
        authenticationInfo.setDataPermission(record.getString("data_permission"));
        authenticationInfo.setOrgId(record.getString("org_id"));
        authenticationInfo.setOrgName(record.getString("org_name"));
        authenticationInfo.setOrgCode(record.getString("org_code"));
        authenticationInfo.setEmail(record.getString("email"));
        authenticationInfo.setPhone(record.getString("phone"));
        authenticationInfo.setPhoto(record.getString("photo"));

        SSOToken token = SSOToken.create()
                .setId(authenticationInfo.getId())
                .setIssuer(Aooms.NAME)
                .setTime(System.currentTimeMillis());
        authenticationInfo.setToken(token.getToken());

        //SSOHelper.setCookie(getRequest(), getResponse(), SSOToken.create().setIp(getRequest()).setId("放用户ID").setIssuer("kisso"), false);
        SSOHelper.setCookie(AoomsContext.getRequest(), AoomsContext.getResponse(),token, false);
        setResultValue(AoomsConstants.Result.Authentication, authenticationInfo);
	}

}