package com.czj.student.service.impl;

import com.czj.student.common.LoginConstants;
import com.czj.student.mapper.LoginMapper;
import com.czj.student.model.entity.Student;
import com.czj.student.model.vo.LoginVO;
import com.czj.student.model.vo.LoginUserVO;
import com.czj.student.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 登录服务实现类
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private LoginMapper loginMapper;

    @Override
    public LoginUserVO login(LoginVO loginVO, HttpSession session) {
        // 1. 检查账号是否锁定
        if (isAccountLocked(loginVO.getSno(), session)) {
            LocalDateTime lockTime = (LocalDateTime) session.getAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
            long remainingMinutes = LoginConstants.LOCK_TIME_MINUTES - ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
            throw new RuntimeException("账号已锁定，请" + remainingMinutes + "分钟后再试");
        }

        // 2. 查询学生信息
        Student student = loginMapper.getStudentBySno(loginVO.getSno());
        if (student == null) {
            throw new RuntimeException("学号不存在");
        }

        // 3. 验证密码
        if (!student.getPwd().equals(loginVO.getPwd())) {
            // 更新数据库中的错误次数
            int errorCount = (student.getLoginErrorCount() == null ? 0 : student.getLoginErrorCount()) + 1;
            loginMapper.updateLoginErrorCount(loginVO.getSno(), errorCount);
            
            // 同时更新session中的错误次数
            session.setAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY, errorCount);
            
            if (errorCount >= LoginConstants.MAX_ERROR_COUNT) {
                // 设置锁定时间
                LocalDateTime lockTime = LocalDateTime.now();
                session.setAttribute(LoginConstants.SESSION_LOCK_TIME_KEY, lockTime);
                // 使用动态计算的剩余时间
                long remainingMinutes = LoginConstants.LOCK_TIME_MINUTES;
                throw new RuntimeException("密码错误次数过多，账号已锁定，请" + remainingMinutes + "分钟后再试");
            }
            
            throw new RuntimeException("密码错误，还剩" + (LoginConstants.MAX_ERROR_COUNT - errorCount) + "次机会");
        }

        // 4. 登录成功，清除错误记录
        session.removeAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
        session.removeAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);

        // 5. 更新登录时间和重置错误次数
        loginMapper.updateLastLoginTime(loginVO.getSno());
        loginMapper.updateLoginErrorCount(loginVO.getSno(), 0);

        // 6. 转换并保存登录信息
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(student, loginUserVO);
        session.setAttribute(LoginConstants.SESSION_USER_KEY, loginUserVO);

        return loginUserVO;
    }

    @Override
    public void logout(HttpSession session) {
        // 清除所有session属性
        session.removeAttribute(LoginConstants.SESSION_USER_KEY);
        session.removeAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
        session.removeAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
        // 使session失效
        session.invalidate();
    }

    @Override
    public LoginUserVO getCurrentUser(HttpSession session) {
        return (LoginUserVO) session.getAttribute(LoginConstants.SESSION_USER_KEY);
    }

    @Override
    public boolean isAccountLocked(String sno, HttpSession session) {
        // 先检查session中的锁定状态
        LocalDateTime lockTime = (LocalDateTime) session.getAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
        if (lockTime != null) {
            // 检查是否超过锁定时间
            long minutes = ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
            if (minutes >= LoginConstants.LOCK_TIME_MINUTES) {
                // 锁定时间已过，清除锁定状态
                session.removeAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
                session.removeAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
                // 重置数据库中的错误次数
                loginMapper.updateLoginErrorCount(sno, 0);
                return false;
            }
            return true;
        }

        // 检查数据库中的错误次数
        Student student = loginMapper.getStudentBySno(sno);
        if (student != null && student.getLoginErrorCount() != null && 
            student.getLoginErrorCount() >= LoginConstants.MAX_ERROR_COUNT) {
            // 如果数据库中的错误次数达到上限，但session中没有锁定时间
            // 说明是新的会话，需要重置错误次数（因为可能已经过了锁定时间）
            loginMapper.updateLoginErrorCount(sno, 0);
            return false;
        }

        return false;
    }
} 