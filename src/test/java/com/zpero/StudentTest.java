package com.zpero;

import com.zpero.entity.Student;
import com.zpero.entity.SysUser;
import com.zpero.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class StudentTest {
     @Autowired
     private StudentMapper studentMapper;

     @Test
     void testInsertStudent(){
          Student student = new Student();
          student.setStudentNo("2023005838");
          student.setName("张三");
          student.setClassId(1L);
          student.setCollegeId(1L);
          student.setCounselorId(1L);
          student.setIdCard("104481200508042816");
          student.setEnrollmentYear("2023");
          student.setStatus("在校");
          studentMapper.insert(student);
     }

     @Test
     void testInsertSysUser(){
          SysUser sysUser = new SysUser();
          sysUser.setUsername("testSysUser1");
          sysUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
          sysUser.setRealName("张三");
     }

}
