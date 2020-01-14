package com.rmall.service.impl;

import com.rmall.common.Const;
import com.rmall.common.ServerResponse;
import com.rmall.dao.UserMapper;
import com.rmall.pojo.User;
import com.rmall.service.IUserService;
import com.rmall.util.MD5Util;
import com.rmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    UserMapper userMapper;

    //登录
    //登录的响应会将user的数据反馈给前台
    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.createByError("用户名不存在");
        }
        //密码登录MD5
        //因为加密的算法都是一样的，所以对传入的password明文加密之后
        //与数据库进行比较来看密码登录是否成功
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        //在数据库中查找用户使用的密码必须得是通过MD5明文加密过后的
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){
            return ServerResponse.createByError("密码错误");
        }

        //到这里说明登录其实已经成功，但是需要把返回前台的json中的密码置空
        //org.apache.commons.lang3.StringUtils
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }


    //注册(普通用户)
    /**
     * 在表单提交之后就会将数据封装为一个User对象
     * */
    public ServerResponse<String> register(User user){
        ServerResponse<String> checkValidResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        //下面的checkValid是当用户名已存在的时候为createByError
        //所以这里的逻辑也应该是错误的时候返回用户名已存在
        if(!checkValidResponse.isSuccess()){
            return checkValidResponse;
        }
        //这里为错误的时候返回邮箱名已被注册
        checkValidResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!checkValidResponse.isSuccess()){
            return checkValidResponse;
        }
        //上面都通过就设置角色权限(普通用户)
        user.setRole(Const.IRole.ROLE_CUSTOMER);

        //MD5明文加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //插入到数据库中
        int resultCount = userMapper.insert(user);

        //int的SQL语句返回的是影响的行数
        if(resultCount == 0){
            return ServerResponse.createByError("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");
    }


    /**
     * 校验
     * */
    public ServerResponse<String> checkValid(String value,String type){
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(value);
                if(resultCount > 0){
                    return ServerResponse.createByError("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(value);
                if(resultCount > 0){
                    return ServerResponse.createByError("email已存在");
                }
            }
        }else {
            //说明类型没有输，为空
            return ServerResponse.createByError("参数错误");
        }
        //满足了以上所有的条件，那么说明想要新建的这个用户在数据库是没有的
        return ServerResponse.createBySuccess("校验成功，可以被注册");
    }

    /**
     * 忘记密码，获取用户的问题
     * */
    public ServerResponse<String> selectUserQuestion(String username){
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccess()){
            //因为checkValid的时候，用户名已存在是createByError，校验成功是用户不存在的情况
            return ServerResponse.createByError("用户不存在");
        }
        String question = userMapper.selectUserQuestion(username);
        if(question != null){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByError("用户没有设置问题");
    }

    /**
     * 验证用户的问题是否正确
     *
     * 验证完用户密码的正确与否就是最后的一步了，所以需要验证是否是相同的用户在修改自己的密码，防止恶意的篡改
     * 使用token保证安全性，先生成后存到缓存中，并传回前端，后面需要再传入此token与缓存中的token进行比较验证是否正确
     * */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount > 0){
            //生成一个UUID序列，然后再考虑将之加到缓存中去
            String forgetPwdToken = UUID.randomUUID().toString();
            //setKey的过程localCache.put(key, value)会把key和对应的token的值加进去tomcat1
            //TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, forgetPwdToken);
            RedisShardedPoolUtil.setEx(Const.TOKEN_PREFIX+username, forgetPwdToken, 60*60*12);
            return ServerResponse.createBySuccess(forgetPwdToken);
        }
        return ServerResponse.createByError("问题的答案错误");
    }

    /**
     * 忘记密码，重置密码
     * */
    //是真要传入通过checkAnswer验证了答案之后返回的token的
    public ServerResponse<String> forgetPwdRestPwd(String username,String passwordNew,String forgetPwdToken){
        //先进行校验
        if(StringUtils.isBlank(forgetPwdToken)){
            return ServerResponse.createByError("参数错误，token需要传递");
        }
        //如果用户名为空的话用户可能就会直接获取token_和forgetToken的内容，对username再进行校验
        ServerResponse ifUsernameIsNullResponse = this.checkValid(username, Const.USERNAME);
        if(ifUsernameIsNullResponse.isSuccess()){
            //因为checkValid的时候，校验成功是用户不存在的情况
            return ServerResponse.createByError("用户不存在");
        }
        //在本地缓存中取TokenCache(已删除).TOKEN_PREFIX + username对应的token的值
        //String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        String token = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX + username);
        //StringUtils的equals（a,b）方法即使a为空也不会报异常，但如果是普通的object的equals方法就会报空指针异常
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByError("token无效或者已过期");
        }
        //需要在此处传进forgetPwdToken来与缓存中的token来比较验证是否是在同一时间段的该用户在修改自己的密码保证安全性
        if(StringUtils.equals(forgetPwdToken, token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            //更新用户的密码
            int resultCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(resultCount > 0 ){
                return ServerResponse.createBySuccess("修改密码成功");
            }
        }else {
            return ServerResponse.createByError("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByError("修改密码失败");
    }

    /**
     * 登录状态下的重置密码
     * */
    public ServerResponse<String> LoginStatusRestPwd(String passwordOld,String passwordNew,User user){
        //防止横向越权，要校验一下这个用户的旧密码，一定要确认是这个用户，避免其未登录被其他用户篡改密码
        //因为我们会查询一个count(1)，如果不指定id，那么结果就是true，count > 0;
        //我想的是有可能有用户密码设置的是一样的，那么查出来的用户就不止一个了,那就不知道修改的是谁的密码了。
        int resultCount = userMapper.LoginStatusCheckPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByError("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        //有更新的话就更新,只更新要更新的
        int updateResultCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateResultCount > 0){
            return ServerResponse.createBySuccess("修改密码成功");
        }
        return ServerResponse.createByError("修改密码失败");
    }


    /**
     * 更新用户的信息
     * */

    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByError("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByError("更新个人信息失败");
    }


    /**
     * 登录状态下获取用户的详细信息给前台
     * */
    public ServerResponse<User> getUserInformation(int userId){
        User currentUser = userMapper.selectByPrimaryKey(userId);
        if(currentUser == null){
            return ServerResponse.createByError("找不到当前用户");
        }
        //由于是要将整个用户的信息交给前台，所以需要将用户的密码置空
        currentUser.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(currentUser);
    }

    /**
     * 校验当前登录的用户是否是管理员（后台）
     * */
    public ServerResponse<String> checkCurrentUserIfAdminUser(User user){
        if(user != null && user.getRole().intValue() == Const.IRole.ROLE_ADMIN){
            return ServerResponse.createBySuccess("当前登录的用户是Admin");
        }
        return ServerResponse.createByError("当前登录用户不为管理员，不具有创建分类的权限");
    }







}
