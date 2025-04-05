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
            throw new RuntimeException("账号已锁定，请15分钟后再试");
        }

        // 2. 查询学生信息
        Student student = loginMapper.getStudentBySno(loginVO.getSno());
        if (student == null) {
            throw new RuntimeException("学号不存在");
        }

        // 3. 验证密码
        if (!student.getPwd().equals(loginVO.getPwd())) {
            // 更新错误次数
            int errorCount = getLoginErrorCount(session) + 1;
            session.setAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY, errorCount);
            
            if (errorCount >= LoginConstants.MAX_ERROR_COUNT) {
                // 设置锁定时间
                session.setAttribute(LoginConstants.SESSION_LOCK_TIME_KEY, LocalDateTime.now());
                throw new RuntimeException("密码错误次数过多，账号已锁定");
            }
            
            throw new RuntimeException("密码错误，还剩" + (LoginConstants.MAX_ERROR_COUNT - errorCount) + "次机会");
        }

        // 4. 登录成功，清除错误记录
        session.removeAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
        session.removeAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);

        // 5. 更新登录时间
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
        LocalDateTime lockTime = (LocalDateTime) session.getAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
        if (lockTime == null) {
            return false;
        }

        // 检查是否超过锁定时间
        long minutes = ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
        if (minutes >= LoginConstants.LOCK_TIME_MINUTES) {
            // 锁定时间已过，清除锁定状态
            session.removeAttribute(LoginConstants.SESSION_LOCK_TIME_KEY);
            session.removeAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
            return false;
        }

        return true;
    }

    /**
     * 获取登录错误次数
     */
    private int getLoginErrorCount(HttpSession session) {
        Integer errorCount = (Integer) session.getAttribute(LoginConstants.SESSION_ERROR_COUNT_KEY);
        return errorCount == null ? 0 : errorCount;
    }
} 