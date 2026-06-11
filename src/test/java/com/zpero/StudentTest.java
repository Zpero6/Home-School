package com.zpero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.Student;
import com.zpero.entity.SysUser;
import com.zpero.mapper.ParentAccountMapper;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @Autowired
    private ParentAccountMapper parentAccountMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    void testParentLogin() throws Exception {
        String idCard = "110101200001010011";
        String password = "123456";

        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getIdCard, idCard)
        );

        if (student == null) {
            student = new Student();
            student.setStudentNo("PLT001");
            student.setName("家长登录测试学生");
            student.setClassId(1L);
            student.setCollegeId(1L);
            student.setCounselorId(1L);
            student.setIdCard(idCard);
            student.setEnrollmentYear("2023");
            student.setStatus("在校");
            studentMapper.insert(student);
        }

        ParentAccount parentAccount = parentAccountMapper.selectOne(
                new LambdaQueryWrapper<ParentAccount>()
                        .eq(ParentAccount::getStudentId, student.getId())
        );

        if (parentAccount == null) {
            parentAccount = new ParentAccount();
            parentAccount.setStudentId(student.getId());
            parentAccount.setUsername(idCard);
            parentAccount.setPassword(passwordEncoder.encode(password));
            parentAccountMapper.insert(parentAccount);
        } else {
            parentAccount.setUsername(idCard);
            parentAccount.setPassword(passwordEncoder.encode(password));
            parentAccountMapper.updateById(parentAccount);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("idCard", idCard);
        map.put("password", password);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/parent/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.parentAccountId").exists())
                .andExpect(jsonPath("$.data.studentId").exists())
                .andExpect(jsonPath("$.data.studentName").value(student.getName()))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
        String token = (String) dataMap.get("token");

        Claims claims = jwtUtil.parseToken(token);
        System.out.println(claims);

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_PARENT"))
                .andExpect(jsonPath("$.data.parentAccountId").value(parentAccount.getId()))
                .andExpect(jsonPath("$.data.studentId").value(student.getId()))
                .andExpect(jsonPath("$.data.realName").value(student.getName()));
    }

    @Test
    void testStudentScoreCrud() throws Exception {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS student_score (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                    student_id BIGINT NOT NULL COMMENT '学生ID',
                    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
                    score DECIMAL(5, 1) NULL COMMENT '分数',
                    academic_year VARCHAR(10) NOT NULL COMMENT '学年',
                    semester TINYINT NOT NULL COMMENT '学期 1/2',
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                    PRIMARY KEY (id),
                    KEY idx_score_student (student_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩'
                """);

        String username = "scoreCounselorTest";
        String password = "123456";

        SysUser counselor = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (counselor == null) {
            counselor = new SysUser();
            counselor.setUsername(username);
            counselor.setRealName("成绩测试辅导员");
            counselor.setRoleId(3L);
            counselor.setCollegeId(1L);
            counselor.setPhone("13800001009");
            counselor.setStatus(1);
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.insert(counselor);
        } else {
            counselor.setRoleId(3L);
            counselor.setCollegeId(counselor.getCollegeId() == null ? 1L : counselor.getCollegeId());
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.updateById(counselor);
        }

        String idCard = "110101200001010022";
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getIdCard, idCard)
        );
        if (student == null) {
            student = new Student();
            student.setStudentNo("SCORE001");
            student.setName("成绩测试学生");
            student.setIdCard(idCard);
            student.setCollegeId(counselor.getCollegeId());
            student.setClassId(1L);
            student.setCounselorId(counselor.getId());
            student.setEnrollmentYear("2023");
            student.setStatus("在校");
            studentMapper.insert(student);
        } else {
            student.setCollegeId(counselor.getCollegeId());
            student.setCounselorId(counselor.getId());
            studentMapper.updateById(student);
        }

        Map<String, Object> loginMap = new HashMap<>();
        loginMap.put("username", username);
        loginMap.put("password", password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        Map<String, Object> loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> loginData = (Map<String, Object>) loginResponse.get("data");
        String token = (String) loginData.get("token");

        String courseName = "高等数学" + System.currentTimeMillis();
        Map<String, Object> createMap = new HashMap<>();
        createMap.put("courseName", courseName);
        createMap.put("score", new BigDecimal("95.5"));
        createMap.put("academicYear", "2025-2026");
        createMap.put("semester", 1);

        MvcResult createResult = mockMvc.perform(post("/api/v1/students/{studentId}/scores", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        Map<String, Object> createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class);
        Long scoreId = ((Number) createResponse.get("data")).longValue();

        String updatedCourseName = courseName + "更新";
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("courseName", updatedCourseName);
        updateMap.put("score", new BigDecimal("88.0"));
        updateMap.put("academicYear", "2025-2026");
        updateMap.put("semester", 2);

        mockMvc.perform(put("/api/v1/scores/{id}", scoreId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMap)))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/v1/students/{studentId}/scores", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> listResponse = objectMapper.readValue(
                listResult.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> scores = (List<Map<String, Object>>) listResponse.get("data");
        assertTrue(scores.stream().anyMatch(score ->
                ((Number) score.get("id")).longValue() == scoreId
                        && updatedCourseName.equals(score.get("courseName"))
                        && ((Number) score.get("semester")).intValue() == 2
        ));

        mockMvc.perform(delete("/api/v1/scores/{id}", scoreId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult deletedListResult = mockMvc.perform(get("/api/v1/students/{studentId}/scores", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> deletedListResponse = objectMapper.readValue(
                deletedListResult.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> deletedScores =
                (List<Map<String, Object>>) deletedListResponse.get("data");
        assertFalse(deletedScores.stream().anyMatch(score ->
                ((Number) score.get("id")).longValue() == scoreId
        ));
    }
}
