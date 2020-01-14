package com.rmall.service;

import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String value,String type);
    ServerResponse<String> selectUserQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String question,String answer);
    ServerResponse<String> forgetPwdRestPwd(String username,String passwordNew,String forgetToken);
    ServerResponse<String> LoginStatusRestPwd(String passwordOld,String passwordNew,User user);
    ServerResponse<User> getUserInformation(int id);
    ServerResponse<String> checkCurrentUserIfAdminUser(User user);
    ServerResponse<User> updateInformation(User user);
}
