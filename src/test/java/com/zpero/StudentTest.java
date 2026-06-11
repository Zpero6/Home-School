package com.zpero;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zpero.entity.ParentAccount;
import com.zpero.entity.Student;
import com.zpero.entity.StudentParent;
import com.zpero.entity.SysUser;
import com.zpero.mapper.ParentAccountMapper;
import com.zpero.mapper.StudentParentMapper;
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
import java.time.LocalDateTime;
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
    private StudentParentMapper studentParentMapper;
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

    @Test
    void testStudentAwardAndCadreCrud() throws Exception {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS student_award (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    student_id BIGINT NOT NULL,
                    award_name VARCHAR(200) NOT NULL,
                    award_level VARCHAR(50) NULL,
                    award_time DATETIME NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                    PRIMARY KEY (id),
                    KEY idx_award_student (student_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS student_cadre (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    student_id BIGINT NOT NULL,
                    position_name VARCHAR(100) NOT NULL,
                    start_time DATETIME NULL,
                    end_time DATETIME NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                    PRIMARY KEY (id),
                    KEY idx_cadre_student (student_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        String username = "awardCadreCounselorTest";
        String password = "123456";

        SysUser counselor = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (counselor == null) {
            counselor = new SysUser();
            counselor.setUsername(username);
            counselor.setRealName("获奖班干部测试辅导员");
            counselor.setRoleId(3L);
            counselor.setCollegeId(1L);
            counselor.setPhone("13800001010");
            counselor.setStatus(1);
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.insert(counselor);
        } else {
            counselor.setRoleId(3L);
            counselor.setCollegeId(counselor.getCollegeId() == null ? 1L : counselor.getCollegeId());
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.updateById(counselor);
        }

        String idCard = "110101200001010033";
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getIdCard, idCard)
        );
        if (student == null) {
            student = new Student();
            student.setStudentNo("AWARD001");
            student.setName("获奖班干部测试学生");
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

        String awardName = "优秀学生干部" + System.currentTimeMillis();
        Map<String, Object> awardCreateMap = new HashMap<>();
        awardCreateMap.put("awardName", awardName);
        awardCreateMap.put("awardLevel", "校级");
        awardCreateMap.put("awardTime", LocalDateTime.of(2026, 6, 1, 10, 0));

        MvcResult awardCreateResult = mockMvc.perform(post("/api/v1/students/{studentId}/awards", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(awardCreateMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long awardId = ((Number) objectMapper.readValue(
                awardCreateResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        String updatedAwardName = awardName + "更新";
        Map<String, Object> awardUpdateMap = new HashMap<>();
        awardUpdateMap.put("awardName", updatedAwardName);
        awardUpdateMap.put("awardLevel", "省级");
        awardUpdateMap.put("awardTime", LocalDateTime.of(2026, 6, 2, 10, 0));

        mockMvc.perform(put("/api/v1/awards/{id}", awardId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(awardUpdateMap)))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult awardListResult = mockMvc.perform(get("/api/v1/students/{studentId}/awards", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> awardListResponse = objectMapper.readValue(
                awardListResult.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> awards = (List<Map<String, Object>>) awardListResponse.get("data");
        assertTrue(awards.stream().anyMatch(award ->
                ((Number) award.get("id")).longValue() == awardId
                        && updatedAwardName.equals(award.get("awardName"))
                        && "省级".equals(award.get("awardLevel"))
        ));

        String positionName = "班长" + System.currentTimeMillis();
        Map<String, Object> cadreCreateMap = new HashMap<>();
        cadreCreateMap.put("positionName", positionName);
        cadreCreateMap.put("startTime", LocalDateTime.of(2025, 9, 1, 0, 0));
        cadreCreateMap.put("endTime", LocalDateTime.of(2026, 7, 1, 0, 0));

        MvcResult cadreCreateResult = mockMvc.perform(post("/api/v1/students/{studentId}/cadres", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadreCreateMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long cadreId = ((Number) objectMapper.readValue(
                cadreCreateResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        String updatedPositionName = positionName + "更新";
        Map<String, Object> cadreUpdateMap = new HashMap<>();
        cadreUpdateMap.put("positionName", updatedPositionName);
        cadreUpdateMap.put("startTime", LocalDateTime.of(2025, 9, 1, 0, 0));
        cadreUpdateMap.put("endTime", LocalDateTime.of(2026, 7, 2, 0, 0));

        mockMvc.perform(put("/api/v1/cadres/{id}", cadreId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadreUpdateMap)))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult cadreListResult = mockMvc.perform(get("/api/v1/students/{studentId}/cadres", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> cadreListResponse = objectMapper.readValue(
                cadreListResult.getResponse().getContentAsString(), Map.class);
        List<Map<String, Object>> cadres = (List<Map<String, Object>>) cadreListResponse.get("data");
        assertTrue(cadres.stream().anyMatch(cadre ->
                ((Number) cadre.get("id")).longValue() == cadreId
                        && updatedPositionName.equals(cadre.get("positionName"))
        ));

        mockMvc.perform(delete("/api/v1/awards/{id}", awardId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cadres/{id}", cadreId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        MvcResult deletedAwardListResult = mockMvc.perform(get("/api/v1/students/{studentId}/awards", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<Map<String, Object>> deletedAwards = (List<Map<String, Object>>) objectMapper.readValue(
                deletedAwardListResult.getResponse().getContentAsString(), Map.class).get("data");
        assertFalse(deletedAwards.stream().anyMatch(award ->
                ((Number) award.get("id")).longValue() == awardId
        ));

        MvcResult deletedCadreListResult = mockMvc.perform(get("/api/v1/students/{studentId}/cadres", student.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<Map<String, Object>> deletedCadres = (List<Map<String, Object>>) objectMapper.readValue(
                deletedCadreListResult.getResponse().getContentAsString(), Map.class).get("data");
        assertFalse(deletedCadres.stream().anyMatch(cadre ->
                ((Number) cadre.get("id")).longValue() == cadreId
        ));
    }

    @Test
    void testLetterTemplateShareAndCopy() throws Exception {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS letter_template (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    content TEXT NOT NULL,
                    background_url VARCHAR(500) NULL,
                    logo_url VARCHAR(300) NULL,
                    creator_id BIGINT NOT NULL,
                    creator_type VARCHAR(20) NOT NULL,
                    college_id BIGINT NULL,
                    is_shared TINYINT(1) NOT NULL DEFAULT 0,
                    source_template_id BIGINT NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                    PRIMARY KEY (id),
                    KEY idx_template_creator (creator_id, creator_type)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        String ownerUsername = "templateOwnerCounselor";
        String copierUsername = "templateCopierCounselor";
        String password = "123456";

        SysUser owner = prepareCounselor(ownerUsername, "模板创建辅导员", password, "13800001101");
        SysUser copier = prepareCounselor(copierUsername, "模板复制辅导员", password, "13800001102");

        String ownerToken = loginAndGetToken(ownerUsername, password);
        String copierToken = loginAndGetToken(copierUsername, password);

        String templateName = "寒假安全告知模板" + System.currentTimeMillis();
        Map<String, Object> createMap = new HashMap<>();
        createMap.put("name", templateName);
        createMap.put("content", "<h1>寒假安全告知</h1><p>${score}</p><p>${award}</p><p>${cadre}</p>");
        createMap.put("backgroundUrl", "/uploads/bg/winter.png");
        createMap.put("logoUrl", "/uploads/logo/school.png");

        MvcResult createResult = mockMvc.perform(post("/api/v1/templates")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long templateId = ((Number) objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        mockMvc.perform(get("/api/v1/templates")
                        .header("Authorization", "Bearer " + ownerToken)
                        .param("name", templateName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(templateId))
                .andExpect(jsonPath("$.data.records[0].creatorId").value(owner.getId()))
                .andExpect(jsonPath("$.data.records[0].creatorType").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.records[0].isShared").value(0));

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", templateName + "更新");
        updateMap.put("content", "<h1>更新后的模板</h1><p>${score}</p>");
        updateMap.put("backgroundUrl", "/uploads/bg/updated.png");
        updateMap.put("logoUrl", "/uploads/logo/updated.png");

        mockMvc.perform(put("/api/v1/templates/{id}", templateId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> shareMap = new HashMap<>();
        shareMap.put("isShared", 1);
        mockMvc.perform(put("/api/v1/templates/{id}/share", templateId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shareMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        MvcResult sharedListResult = mockMvc.perform(get("/api/v1/templates/shared")
                        .header("Authorization", "Bearer " + copierToken)
                        .param("name", templateName + "更新")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> sharedListResponse = objectMapper.readValue(
                sharedListResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> sharedListData = (Map<String, Object>) sharedListResponse.get("data");
        List<Map<String, Object>> sharedTemplates =
                (List<Map<String, Object>>) sharedListData.get("records");
        assertTrue(sharedTemplates.stream().anyMatch(template ->
                ((Number) template.get("id")).longValue() == templateId
                        && ((Number) template.get("isShared")).intValue() == 1
        ));

        MvcResult copyResult = mockMvc.perform(post("/api/v1/templates/{id}/copy", templateId)
                        .header("Authorization", "Bearer " + copierToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long copiedTemplateId = ((Number) objectMapper.readValue(
                copyResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        mockMvc.perform(get("/api/v1/templates/{id}", copiedTemplateId)
                        .header("Authorization", "Bearer " + copierToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.creatorId").value(copier.getId()))
                .andExpect(jsonPath("$.data.sourceTemplateId").value(templateId))
                .andExpect(jsonPath("$.data.isShared").value(0));

        mockMvc.perform(put("/api/v1/templates/{id}", templateId)
                        .header("Authorization", "Bearer " + copierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(delete("/api/v1/templates/{id}", templateId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/v1/templates/{id}", copiedTemplateId)
                        .header("Authorization", "Bearer " + copierToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testSendLetters() throws Exception {
        String username = "letterSendCounselor";
        String password = "123456";

        SysUser counselor = prepareCounselor(username, "信件发送测试辅导员", password, "13800001201");
        String token = loginAndGetToken(username, password);

        Student successStudent = prepareStudent(
                "LETTER001",
                "信件发送成功学生",
                "110101200001010044",
                counselor.getId(),
                counselor.getCollegeId()
        );
        Student failStudent = prepareStudent(
                "LETTER002",
                "信件发送失败学生",
                "110101200001010055",
                counselor.getId(),
                counselor.getCollegeId()
        );
        prepareParent(successStudent.getId(), "成功学生家长", "FATHER", "13900001201", 1);
        prepareParent(failStudent.getId(), "失败学生家长", "MOTHER", null, 1);

        jdbcTemplate.update("""
                        INSERT INTO student_score (student_id, course_name, score, academic_year, semester)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                successStudent.getId(),
                "信件测试课程",
                new BigDecimal("96.0"),
                "2025-2026",
                1);
        jdbcTemplate.update("""
                        INSERT INTO student_award (student_id, award_name, award_level, award_time)
                        VALUES (?, ?, ?, ?)
                        """,
                successStudent.getId(),
                "信件测试奖项",
                "校级",
                LocalDateTime.of(2026, 6, 1, 10, 0));
        jdbcTemplate.update("""
                        INSERT INTO student_cadre (student_id, position_name, start_time, end_time)
                        VALUES (?, ?, ?, ?)
                        """,
                successStudent.getId(),
                "信件测试班干部",
                LocalDateTime.of(2025, 9, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0));

        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("name", "信件发送模板" + System.currentTimeMillis());
        templateMap.put("content", "<h1>学生在校情况</h1>${score}${award}${cadre}");
        templateMap.put("backgroundUrl", "/uploads/bg/letter.png");
        templateMap.put("logoUrl", "/uploads/logo/letter.png");

        MvcResult templateResult = mockMvc.perform(post("/api/v1/templates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long templateId = ((Number) objectMapper.readValue(
                templateResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("templateId", templateId);
        sendMap.put("studentIds", List.of(successStudent.getId(), failStudent.getId()));

        MvcResult sendResult = mockMvc.perform(post("/api/v1/letters/send")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failCount").value(1))
                .andExpect(jsonPath("$.data.failList[0].studentId").value(failStudent.getId()))
                .andReturn();

        System.out.println(sendResult.getResponse().getContentAsString());

        MvcResult listResult = mockMvc.perform(get("/api/v1/letters")
                        .header("Authorization", "Bearer " + token)
                        .param("studentId", String.valueOf(successStudent.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].status").value("SENT"))
                .andReturn();

        Map<String, Object> listResponse = objectMapper.readValue(
                listResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> listData = (Map<String, Object>) listResponse.get("data");
        List<Map<String, Object>> letters = (List<Map<String, Object>>) listData.get("records");
        Map<String, Object> sentLetter = letters.get(0);
        Long letterId = ((Number) sentLetter.get("id")).longValue();
        String content = (String) sentLetter.get("content");
        assertTrue(content.contains("信件测试课程"));
        assertTrue(content.contains("信件测试奖项"));
        assertTrue(content.contains("信件测试班干部"));

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("content", content + "<p>辅导员补充说明</p>");
        mockMvc.perform(put("/api/v1/letters/{id}", letterId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Map<String, Object> resendMap = new HashMap<>();
        resendMap.put("letterIds", List.of(letterId));
        mockMvc.perform(post("/api/v1/letters/resend")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failCount").value(0));

        Integer successSmsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sms_record WHERE student_id = ? AND status = 'SUCCESS'",
                Integer.class,
                successStudent.getId()
        );
        Integer failSmsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sms_record WHERE student_id = ? AND status = 'FAIL'",
                Integer.class,
                failStudent.getId()
        );
        Integer parentAccountCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parent_account WHERE student_id = ?",
                Integer.class,
                successStudent.getId()
        );
        assertTrue(successSmsCount != null && successSmsCount >= 2);
        assertTrue(failSmsCount != null && failSmsCount >= 1);
        assertTrue(parentAccountCount != null && parentAccountCount >= 1);

        String schoolUsername = "smsRecordSchool";
        String schoolPassword = "123456";
        prepareRoleUser(schoolUsername, "短信记录学校用户", schoolPassword, "13800001299", 1L, counselor.getCollegeId());
        String schoolToken = loginAndGetToken(schoolUsername, schoolPassword);

        mockMvc.perform(get("/api/v1/sms-records")
                        .header("Authorization", "Bearer " + schoolToken)
                        .param("studentId", String.valueOf(successStudent.getId()))
                        .param("status", "SUCCESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(successSmsCount))
                .andExpect(jsonPath("$.data.records[0].studentId").value(successStudent.getId()))
                .andExpect(jsonPath("$.data.records[0].studentName").value(successStudent.getName()))
                .andExpect(jsonPath("$.data.records[0].parentName").value("成功学生家长"))
                .andExpect(jsonPath("$.data.records[0].status").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/sms-records")
                        .header("Authorization", "Bearer " + token)
                        .param("studentId", String.valueOf(successStudent.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/sms-records/student/{studentId}", failStudent.getId())
                        .header("Authorization", "Bearer " + schoolToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].studentId").value(failStudent.getId()))
                .andExpect(jsonPath("$.data[0].studentName").value(failStudent.getName()))
                .andExpect(jsonPath("$.data[0].status").value("FAIL"))
                .andExpect(jsonPath("$.data[0].failReason").value("家长联系电话为空"));
    }

    @Test
    void testParentReadLetterAndSubmitFeedback() throws Exception {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS parent_feedback (
                    id BIGINT NOT NULL AUTO_INCREMENT,
                    letter_id BIGINT NOT NULL,
                    student_id BIGINT NOT NULL,
                    parent_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    images VARCHAR(1000) NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id),
                    KEY idx_feedback_letter (letter_id),
                    KEY idx_feedback_student (student_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

        String suffix = String.valueOf(System.currentTimeMillis()).substring(7);
        String counselorUsername = "parentLetterCounselor" + suffix;
        String counselorPassword = "123456";
        SysUser counselor = prepareCounselor(
                counselorUsername,
                "家长查阅测试辅导员",
                counselorPassword,
                "13800" + suffix
        );
        String counselorToken = loginAndGetToken(counselorUsername, counselorPassword);

        String idCard = "110101200001" + suffix;
        Student student = prepareStudent(
                "PARENTLETTER" + suffix + "01",
                "家长查阅测试学生",
                idCard,
                counselor.getId(),
                counselor.getCollegeId()
        );
        prepareParent(student.getId(), "查阅测试家长", "FATHER", "13900" + suffix, 1);

        String unreadIdCard = "110101200002" + suffix;
        Student unreadStudent = prepareStudent(
                "PARENTLETTER" + suffix + "02",
                "未查阅测试学生",
                unreadIdCard,
                counselor.getId(),
                counselor.getCollegeId()
        );
        prepareParent(unreadStudent.getId(), "未查阅测试家长", "MOTHER", "13901" + suffix, 1);

        Student unsentStudent = prepareStudent(
                "PARENTLETTER" + suffix + "03",
                "未发送测试学生",
                "110101200003" + suffix,
                counselor.getId(),
                counselor.getCollegeId()
        );

        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("name", "家长查阅测试模板" + System.currentTimeMillis());
        templateMap.put("content", "<h1>家长端信件</h1><p>请查收</p>");
        templateMap.put("backgroundUrl", "/uploads/bg/parent-letter.png");
        templateMap.put("logoUrl", "/uploads/logo/parent-letter.png");

        MvcResult templateResult = mockMvc.perform(post("/api/v1/templates")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        Long templateId = ((Number) objectMapper.readValue(
                templateResult.getResponse().getContentAsString(), Map.class).get("data")).longValue();

        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("templateId", templateId);
        sendMap.put("studentIds", List.of(student.getId(), unreadStudent.getId()));
        mockMvc.perform(post("/api/v1/letters/send")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failCount").value(0));

        Map<String, Object> parentLoginMap = new HashMap<>();
        parentLoginMap.put("idCard", idCard);
        parentLoginMap.put("password", idCard.substring(idCard.length() - 6));
        MvcResult parentLoginResult = mockMvc.perform(post("/api/v1/parent/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentLoginMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();
        Map<String, Object> parentLoginResponse = objectMapper.readValue(
                parentLoginResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> parentLoginData = (Map<String, Object>) parentLoginResponse.get("data");
        String parentToken = (String) parentLoginData.get("token");

        MvcResult letterResult = mockMvc.perform(get("/api/v1/parent/letter")
                        .header("Authorization", "Bearer " + parentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(student.getId()))
                .andExpect(jsonPath("$.data.studentName").value(student.getName()))
                .andExpect(jsonPath("$.data.content").value("<h1>家长端信件</h1><p>请查收</p>"))
                .andExpect(jsonPath("$.data.status").value("READ"))
                .andExpect(jsonPath("$.data.readTime").exists())
                .andExpect(jsonPath("$.data.backgroundUrl").value("/uploads/bg/parent-letter.png"))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/logo/parent-letter.png"))
                .andReturn();

        Map<String, Object> letterResponse = objectMapper.readValue(
                letterResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> letterData = (Map<String, Object>) letterResponse.get("data");
        Long letterId = ((Number) letterData.get("letterId")).longValue();

        String feedbackContent = "已收到家长端信件" + System.currentTimeMillis();
        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("letterId", letterId);
        feedbackMap.put("content", feedbackContent);
        feedbackMap.put("images", "/uploads/feedback/parent-letter.png");

        mockMvc.perform(post("/api/v1/parent/feedback")
                        .header("Authorization", "Bearer " + parentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackMap)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        Integer readCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_letter WHERE id = ? AND status = 'READ' AND read_time IS NOT NULL",
                Integer.class,
                letterId
        );
        Integer feedbackCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM parent_feedback WHERE letter_id = ? AND student_id = ? AND content = ?",
                Integer.class,
                letterId,
                student.getId(),
                feedbackContent
        );

        assertTrue(readCount != null && readCount == 1);
        assertTrue(feedbackCount != null && feedbackCount == 1);

        mockMvc.perform(get("/api/v1/feedbacks")
                        .header("Authorization", "Bearer " + counselorToken)
                        .param("content", feedbackContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].letterId").value(letterId))
                .andExpect(jsonPath("$.data.records[0].studentId").value(student.getId()))
                .andExpect(jsonPath("$.data.records[0].studentName").value(student.getName()))
                .andExpect(jsonPath("$.data.records[0].parentName").value("查阅测试家长"))
                .andExpect(jsonPath("$.data.records[0].content").value(feedbackContent));

        mockMvc.perform(get("/api/v1/statistics/read")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldSendCount").value(3))
                .andExpect(jsonPath("$.data.actualSendCount").value(2))
                .andExpect(jsonPath("$.data.readCount").value(1))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.readRate").value(50.00));

        MvcResult feedbackStatisticsResult = mockMvc.perform(get("/api/v1/statistics/feedback")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldSendCount").value(3))
                .andExpect(jsonPath("$.data.actualSendCount").value(2))
                .andExpect(jsonPath("$.data.feedbackStudentCount").value(1))
                .andExpect(jsonPath("$.data.noFeedbackCount").value(1))
                .andExpect(jsonPath("$.data.feedbackRate").value(50.00))
                .andReturn();
        Map<String, Object> feedbackStatisticsResponse = objectMapper.readValue(
                feedbackStatisticsResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> feedbackStatisticsData =
                (Map<String, Object>) feedbackStatisticsResponse.get("data");
        assertTrue(((Number) feedbackStatisticsData.get("totalFeedbackCount")).longValue() >= 1);

        mockMvc.perform(get("/api/v1/statistics/send/class")
                        .header("Authorization", "Bearer " + counselorToken)
                        .param("classId", String.valueOf(student.getClassId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("CLASS"))
                .andExpect(jsonPath("$.data.summary.shouldSendCount").value(3))
                .andExpect(jsonPath("$.data.summary.actualSendCount").value(2))
                .andExpect(jsonPath("$.data.summary.unsentCount").value(1))
                .andExpect(jsonPath("$.data.summary.completionRate").value(66.67))
                .andExpect(jsonPath("$.data.records[0].targetId").value(student.getClassId()));

        mockMvc.perform(get("/api/v1/statistics/unread")
                        .header("Authorization", "Bearer " + counselorToken)
                        .param("classId", String.valueOf(student.getClassId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].studentId").value(unreadStudent.getId()))
                .andExpect(jsonPath("$.data.records[0].studentName").value(unreadStudent.getName()))
                .andExpect(jsonPath("$.data.records[0].sendStatus").value("SENT"))
                .andExpect(jsonPath("$.data.records[0].readStatus").value("UNREAD"))
                .andExpect(jsonPath("$.data.records[0].letterId").exists());

        mockMvc.perform(get("/api/v1/statistics/unsent")
                        .header("Authorization", "Bearer " + counselorToken)
                        .param("classId", String.valueOf(student.getClassId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].studentId").value(unsentStudent.getId()))
                .andExpect(jsonPath("$.data.records[0].studentName").value(unsentStudent.getName()))
                .andExpect(jsonPath("$.data.records[0].sendStatus").value("UNSENT"));

        String collegeUsername = "sendStatisticsCollege";
        String schoolUsername = "sendStatisticsSchool";
        String statisticsPassword = "123456";
        prepareRoleUser(collegeUsername, "发送统计学院用户", statisticsPassword, "13800001302", 2L, counselor.getCollegeId());
        prepareRoleUser(schoolUsername, "发送统计学校用户", statisticsPassword, "13800001303", 1L, counselor.getCollegeId());
        String collegeToken = loginAndGetToken(collegeUsername, statisticsPassword);
        String schoolToken = loginAndGetToken(schoolUsername, statisticsPassword);

        mockMvc.perform(get("/api/v1/statistics/send/college")
                        .header("Authorization", "Bearer " + collegeToken)
                        .param("counselorId", String.valueOf(counselor.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.summary.shouldSendCount").value(3))
                .andExpect(jsonPath("$.data.summary.actualSendCount").value(2))
                .andExpect(jsonPath("$.data.summary.unsentCount").value(1))
                .andExpect(jsonPath("$.data.summary.completionRate").value(66.67))
                .andExpect(jsonPath("$.data.records[0].targetId").value(counselor.getId()));

        mockMvc.perform(get("/api/v1/statistics/send/school")
                        .header("Authorization", "Bearer " + schoolToken)
                        .param("collegeId", String.valueOf(counselor.getCollegeId()))
                        .param("counselorId", String.valueOf(counselor.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("COLLEGE"))
                .andExpect(jsonPath("$.data.summary.shouldSendCount").value(3))
                .andExpect(jsonPath("$.data.summary.actualSendCount").value(2))
                .andExpect(jsonPath("$.data.summary.unsentCount").value(1))
                .andExpect(jsonPath("$.data.summary.completionRate").value(66.67))
                .andExpect(jsonPath("$.data.records[0].targetId").value(counselor.getCollegeId()));

        MvcResult sendExportResult = mockMvc.perform(get("/api/v1/statistics/send/export")
                        .header("Authorization", "Bearer " + collegeToken)
                        .param("counselorId", String.valueOf(counselor.getId())))
                .andExpect(status().isOk())
                .andReturn();
        assertExcelResponse(sendExportResult, "send-statistics.xlsx");

        MvcResult readExportResult = mockMvc.perform(get("/api/v1/statistics/read/export")
                        .header("Authorization", "Bearer " + collegeToken)
                        .param("counselorId", String.valueOf(counselor.getId())))
                .andExpect(status().isOk())
                .andReturn();
        assertExcelResponse(readExportResult, "read-statistics.xlsx");

        MvcResult feedbackExportResult = mockMvc.perform(get("/api/v1/statistics/feedback/export")
                        .header("Authorization", "Bearer " + collegeToken)
                        .param("counselorId", String.valueOf(counselor.getId())))
                .andExpect(status().isOk())
                .andReturn();
        assertExcelResponse(feedbackExportResult, "feedback-statistics.xlsx");
    }

    private SysUser prepareCounselor(String username,
                                     String realName,
                                     String password,
                                     String phone) {
        return prepareRoleUser(username, realName, password, phone, 3L, 1L);
    }

    private SysUser prepareRoleUser(String username,
                                    String realName,
                                    String password,
                                    String phone,
                                    Long roleId,
                                    Long collegeId) {
        SysUser counselor = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        if (counselor == null) {
            counselor = new SysUser();
            counselor.setUsername(username);
            counselor.setRealName(realName);
            counselor.setRoleId(roleId);
            counselor.setCollegeId(collegeId);
            counselor.setPhone(phone);
            counselor.setStatus(1);
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.insert(counselor);
        } else {
            counselor.setRealName(realName);
            counselor.setRoleId(roleId);
            counselor.setCollegeId(collegeId);
            counselor.setPhone(phone);
            counselor.setStatus(1);
            counselor.setPassword(passwordEncoder.encode(password));
            sysUserMapper.updateById(counselor);
        }
        return counselor;
    }

    private String loginAndGetToken(String username, String password) throws Exception {
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
        return (String) loginData.get("token");
    }

    private void assertExcelResponse(MvcResult result, String filename) {
        String contentType = result.getResponse().getContentType();
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        byte[] bytes = result.getResponse().getContentAsByteArray();

        assertTrue(contentType != null
                && contentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(contentDisposition != null && contentDisposition.contains(filename));
        assertTrue(bytes.length > 0);
    }

    private Student prepareStudent(String studentNo,
                                   String name,
                                   String idCard,
                                   Long counselorId,
                                   Long collegeId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getIdCard, idCard)
        );
        if (student == null) {
            student = new Student();
            student.setStudentNo(studentNo);
            student.setName(name);
            student.setIdCard(idCard);
            student.setCollegeId(collegeId);
            student.setClassId(1L);
            student.setCounselorId(counselorId);
            student.setEnrollmentYear("2023");
            student.setStatus("在校");
            studentMapper.insert(student);
        } else {
            student.setStudentNo(studentNo);
            student.setName(name);
            student.setCollegeId(collegeId);
            student.setClassId(1L);
            student.setCounselorId(counselorId);
            student.setEnrollmentYear("2023");
            student.setStatus("在校");
            studentMapper.updateById(student);
        }
        return student;
    }

    private StudentParent prepareParent(Long studentId,
                                        String name,
                                        String relation,
                                        String phone,
                                        Integer isDefault) {
        StudentParent parent = studentParentMapper.selectOne(
                new LambdaQueryWrapper<StudentParent>()
                        .eq(StudentParent::getStudentId, studentId)
                        .eq(StudentParent::getRelation, relation)
        );
        if (parent == null) {
            parent = new StudentParent();
            parent.setStudentId(studentId);
            parent.setRelation(relation);
            parent.setSourceType("MANUAL");
        }
        parent.setName(name);
        parent.setPhone(phone);
        parent.setIsDefault(isDefault);
        if (parent.getId() == null) {
            studentParentMapper.insert(parent);
        } else {
            studentParentMapper.updateById(parent);
        }
        return parent;
    }
}
