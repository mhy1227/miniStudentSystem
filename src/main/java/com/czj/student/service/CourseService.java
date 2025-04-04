package com.czj.student.service;

import org.springframework.stereotype.Service;
import com.czj.student.model.entity.Course;
import com.czj.student.util.PageRequest;
import com.czj.student.util.PageResult;

/**
 * 课程服务接口
 */
@Service
public interface CourseService {
  
  /**
   * 分页查询课程列表
   * @param course 查询条件
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  PageResult<Course> listCourses(Course course, PageRequest pageRequest);

  /**
   * 根据ID查询课程
   * @param cid 课程ID
   * @return 课程信息
   */
  Course getCourseById(Long cid);

  /**
   * 根据课程编号查询课程
   * @param courseNo 课程编号
   * @return 课程信息
   */
  Course getCourseByCourseNo(String courseNo);

  /**
   * 新增课程
   * @param course 课程信息
   */
  void addCourse(Course course);  

  /**
   * 更新课程信息
   * @param course 课程信息
   */
  void updateCourse(Course course);   

  /**
   * 删除课程
   * @param cid 课程ID
   */
  void deleteCourse(Long cid);        

  /**
   * 批量删除课程
   * @param cids 课程ID列表
   */
  // void deleteCourses(List<Long> cids);
}
