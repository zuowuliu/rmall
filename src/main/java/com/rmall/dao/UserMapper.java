package com.rmall.dao;

import com.rmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    User selectLogin(@Param("username") String username, @Param("password") String password);

    int checkEmail(String email);

    String selectUserQuestion(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username,@Param("passwordNew") String passwordNew);

    int LoginStatusCheckPassword(@Param("password") String password,@Param("userId") Integer userId);

    int checkEmailByUserId(@Param(value="email")String email,@Param(value="userId")Integer userId);
}