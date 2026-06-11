package com.zpero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpero.entity.Student;
import com.zpero.entity.SysUser;
import com.zpero.mapper.StudentMapper;
import com.zpero.mapper.SysUserMapper;
import com.zpero.security.JwtUtil;
import com.zpero.security.UserDetailServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 启用 MockMvc 自动配置
public class StudentTest {
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testInsertStudent() {
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
    void testInsertSysUser() {
        SysUser sysUser = new SysUser();
        sysUser.setUsername("testSysUser1");
        sysUser.setPassword(new BCryptPasswordEncoder().encode("123456"));
        sysUser.setRealName("张三");
        sysUser.setRoleId(1L);
        sysUser.setCollegeId(1L);
        sysUser.setPhone("13800000000");
        sysUserMapper.insert(sysUser);
    }

    @Test
    void testLoginUser() {
        UserDetails userDetails = userDetailService.loadUserByUsername("testSysUser1");
        System.out.println(userDetails);


    }

    @Test
    void testTokenParse() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "testSysUser1");
        map.put("password", "123456");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

        String token = (String) dataMap.get("token");
        Claims claims = jwtUtil.parseToken(token);
        System.out.println(claims);
    }

    @Test
    void testMeWithoutToken() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();
        System.out.println(mvcResult.getResponse().getStatus());
    }

    @Test
    void testWithoutAuthorization() throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("username", "Counselor1");
        map.put("password", "123456");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
        String token = (String) dataMap.get("token");

        MvcResult mvcResult1 = mockMvc.perform(get("/school")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andReturn();

        System.out.println(mvcResult1.getResponse().getContentAsString());
    }
}
