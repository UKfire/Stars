package com.ytying.service;

import com.ytying.entity.UserCode;

import java.util.List;

/**
 * Created by kefan.wkf on 17/1/30.
 */
public interface UserCodeService {

    int addUserCode(UserCode userCode);

    List<UserCode> getUserCodeList(int uid);

    boolean removeUserCode(int id,int uid);

}
