# 家校互通系统 需求分析文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档版本 | v3.2 |
| 创建日期 | 2026-06-05 |
| 项目名称 | 家校互通系统 (Home-School) |
| 技术栈 | Java 21 + Spring Boot 3.5.14 + MyBatis-Plus 3.5 + MySQL 8.0 + Spring Security |
| 文档状态 | 已确认 |

---

## 1. 项目概述

### 1.1 项目背景

每年寒暑假，学校学生管理办公室需要向学生家长发送信件，内容包含安全告知书、学生成绩及其他通知事项。当前流程依赖人工操作，效率低、易出错、无法追踪。

本系统提供基于 Web 的集中化管理平台，实现信件模板化编辑、批量发送、家长查阅与反馈的闭环。

### 1.2 项目目标

1. 实现信件内容的模板化管理，支持富文本编辑
2. 实现信件的批量发送与发送状态追踪
3. 为家长提供 H5 端查看信件和反馈的渠道
4. 提供发送率和查阅率的三级统计看板
5. 数据自建自管，不依赖外部系统（实训性质）
6. 基于 Spring Security RBAC 实现细粒度角色权限控制

### 1.3 适用范围

**做：**
- PC 管理后台：模板管理、信件编辑、批量发送、三级统计
- H5 家长端：信件查看、密码管理、意见反馈
- 本地数据管理：学生、家长、成绩的 CRUD
- 短信发送模拟（本地记录 + 状态标记）
- Spring Security + JWT 双通道认证
- RBAC 角色权限模型（4 种角色 + 方法级权限控制）
- 外部链接管理、背景管理

**不做：**
- 不对接真实短信平台（实训项目采用本地模拟）
- 不依赖外部系统数据导入（本地自建数据）
- 不做 App 端
- 不做工作流审批（模板共享直接可用，无需审批）
- 不做动态菜单权限（角色固定为 4 种，权限树静态配置）

### 1.4 术语定义

| 术语 | 说明 |
|------|------|
| 信件 | 发送给家长的寒暑假通知书，包含安全告知书、成绩、其他内容 |
| 模板 | 预设的信件内容格式（HTML 富文本），支持共享给下级用户 |
| 发送率 | 已发送学生数 / 应发送学生数 |
| 查阅率 | 家长已查阅数 / 已发送数 |
| 反馈 | 家长查看信件后可提交的文字 + 图片回复 |
| RBAC | 基于角色的访问控制，用户 → 角色 → 权限 |

---

## 2. 技术方案（总览）

### 2.1 技术选型

| 层次 | 技术 | 说明 |
|------|------|------|
| JDK | Java 21 | |
| 后端框架 | Spring Boot 3.5.14 | 内嵌 Tomcat |
| ORM | MyBatis-Plus 3.5.9 | mybatis-plus-spring-boot3-starter |
| 数据库 | MySQL 8.0 | 本地实例 |
| 认证授权 | Spring Security 6 + JWT + RBAC | PC 端 + H5 端双通道，方法级权限 |
| 缓存/Token 存储 | Redis 7 + Lettuce | JWT 持久化存储，支持注销、黑名单、会话管理 |
| API 文档 | Knife4j (OpenAPI 3) | 开发阶段使用 |
| 构建工具 | Maven 3.9+ | |
| 富文本编辑器 | TinyMCE（前端） | 支持 Word 粘贴保留格式 |
| 文件存储 | 本地文件系统 | 图片上传到 `uploads/` 目录 |

> Redis 作为 JWT Token 的持久化存储层，支持主动注销、Token 黑名单和会话管理。

### 2.2 关键设计决策

| 决策 | 说明 |
|------|------|
| 权限模型 | RBAC：`sys_role` 定义角色，`sys_user.role_id` 关联角色，方法级 `@PreAuthorize` 控制 |
| 短信发送 | 本地模拟：`sms_record` 表记录短信发送状态，不调用真实接口 |
| 家长登录 | 一个家庭一个账号，独立 `parent_account` 表，使用学生身份证号登录，密码 BCrypt 加密 |
| 模板共享 | 复制模式：上级共享后，下级复制到自己的模板库，修改互不影响 |
| JWT 存储 | Redis 存储 JWT Token，支持主动注销（踢人）、Token 黑名单、过期自动清理 |
| 会话管理 | 同一账号后登录者不踢前者（Token 并存），管理员可通过 Redis 手动撤销指定 Token |
| 成绩管理 | 独立 `student_score` 表，辅导员手动录入或 Excel 批量导入 |
| 学院管理 | 独立 `college` 表，学校用户可增删改查 |
| 统计查询 | 实时查询，不做预计算 |
| 家长账号 | 独立 `parent_account` 表，与学生一对一绑定，不与默认联系人耦合 |
| 信件快照 | 发送时生成信件内容快照，模板修改不影响已发送信件 |
| 系统配置 | `system_config` 表 key-value 存储，背景图/Logo/H5地址等统一配置，无需改模板 |
| 数据导出 | EasyExcel 直接查询导出，不依赖预计算 |

### 2.3 项目结构

```
src/main/java/com/zpero/
├── HomeSchoolApplication.java         # Spring Boot 启动类
├── controller/                        # REST 接口层
│   ├── HelloController.java           # [已实现] 测试接口
│   ├── AuthController.java            # [待实现] 认证接口
│   ├── UserController.java            # [待实现] 用户管理
│   ├── StudentController.java         # [待实现] 学生管理
│   ├── TemplateController.java        # [待实现] 模板管理
│   ├── LetterController.java          # [待实现] 信件发送
│   ├── StatisticsController.java      # [待实现] 统计查询
│   ├── ParentController.java          # [待实现] 家长端接口
│   └── ExternalLinkController.java    # [待实现] 外部链接管理
├── service/
│   └── impl/                          # [待实现] 业务逻辑层
├── mapper/                            # MyBatis-Plus Mapper
│   └── StudentMapper.java             # [已实现]
├── entity/                            # 数据库实体
│   ├── baseEntity/
│   │   └── BaseEntity.java            # [已实现] 实体基类
│   ├── SysRole.java                   # [已实现]
│   ├── SysUser.java                   # [已实现]
│   ├── College.java                   # [已实现]
│   ├── ClassInfo.java                 # [已实现]
│   ├── Student.java                   # [已实现]
│   ├── StudentParent.java             # [已实现]
│   ├── ParentAccount.java             # [已实现]
│   ├── StudentScore.java              # [已实现]
│   ├── StudentAward.java              # [已实现]
│   ├── StudentCadre.java              # [已实现]
│   ├── LetterTemplate.java            # [已实现]
│   ├── StudentLetter.java             # [已实现]
│   ├── SmsRecord.java                 # [已实现]
│   ├── ParentFeedback.java            # [已实现]
│   ├── ExternalLink.java              # [已实现]
│   └── SystemConfig.java              # [已实现]
├── dto/                               # [待实现] 请求参数对象
├── vo/                                # [待实现] 视图对象
├── config/
│   ├── JwtAuthenticationFilter.java   # [已实现] JWT 认证过滤器
│   ├── MybatisPlusConfig.java         # [已实现] MybatisPlus 配置
│   ├── MyMetaObjectHandler.java       # [已实现] 自动填充处理器
│   ├── RedisConfig.java               # [待实现] Redis 配置
│   └── WebMvcConfig.java              # [待实现] Web MVC 配置
├── security/
│   ├── SecurityConfig.java            # [已实现] Spring Security 配置
│   ├── JwtUtil.java                   # [已实现] JWT 工具类
│   ├── JwtTokenService.java           # [待实现] Redis Token 管理服务
│   ├── UserDetailServiceImpl.java     # [已实现] 用户详情服务
│   └── PermissionEvaluator.java       # [待实现] 权限表达式求值
├── common/
│   ├── constant/
│   │   └── SecurityConstant.java      # [已实现] 安全常量
│   ├── exception/
│   │   ├── BusinessException.java     # [已实现]
│   │   └── GlobalExceptionHandler.java # [已实现]
│   └── result/
│       ├── Result.java                # [已实现]
│       ├── ResultCode.java            # [已实现]
│       └── PageResult.java            # [已实现]
└── util/
    └── SmsSimulator.java              # [待实现] 短信模拟工具
```

---

## 3. 用户角色与权限设计

### 3.1 RBAC 角色定义

| 角色 | 角色编码 | 端 | 典型用户 | 权限层级 |
|------|---------|------|----------|:---:|
| 学校用户 | `ROLE_SCHOOL` | PC | 学工部老师 | 最高 |
| 学院用户 | `ROLE_COLLEGE` | PC | 学院学工办老师 | 本院 |
| 辅导员 | `ROLE_COUNSELOR` | PC | 班级辅导员 | 本班 |
| 家长 | `ROLE_PARENT` | H5 | 学生家长 | 本人 |

### 3.2 角色层级关系

```
ROLE_SCHOOL
    ↓ (可管理学院用户、查看全校数据)
ROLE_COLLEGE
    ↓ (可管理本学院辅导员、查看本院数据)
ROLE_COUNSELOR
    (管理本班学生和家长、发送信件)

ROLE_PARENT (独立通道，仅 H5 端)
```

### 3.3 权限矩阵

| 功能模块 | 学校用户 | 学院用户 | 辅导员 | 家长 |
|----------|:---:|:---:|:---:|:---:|
| 学院管理 | ✅ | ❌ | ❌ | ❌ |
| 班级管理 | ✅ | ✅ | ❌ | ❌ |
| 用户管理（创建学院/辅导员账号） | ✅ | ❌ | ❌ | ❌ |
| 外部链接管理 | ✅ | ❌ | ❌ | ❌ |
| 背景管理 | ✅ | ✅ | ❌ | ❌ |
| 学生信息管理 | ✅ | ✅(本院) | ✅(本班) | ❌ |
| 家长信息管理 | ✅ | ✅(本院) | ✅(本班) | ❌ |
| 成绩录入 | ❌ | ❌ | ✅(本班) | ❌ |
| 模板管理 | ✅ | ✅ | ✅ | ❌ |
| 模板共享 | ✅ | ✅ | ✅ | ❌ |
| 信件编辑与发送 | ❌ | ❌ | ✅ | ❌ |
| 重新发送 | ❌ | ❌ | ✅ | ❌ |
| 发送统计 | ✅(全校) | ✅(本院) | ✅(本班) | ❌ |
| 查阅统计 | ✅ | ✅ | ✅ | ❌ |
| 反馈统计 | ✅ | ✅ | ✅ | ❌ |
| 短信记录查看 | ✅ | ❌ | ❌ | ❌ |
| 查看信件 | ❌ | ❌ | ❌ | ✅ |
| 提交反馈 | ❌ | ❌ | ❌ | ✅ |
| 修改密码 | ✅ | ✅ | ✅ | ✅ |

### 3.4 Spring Security RBAC 配置设计

#### 3.4.1 核心组件

| 组件 | 类 | 说明 |
|------|-----|------|
| 认证过滤器 | `JwtAuthenticationFilter` | 从 Header 提取 JWT，校验 Redis 中 Token 有效性并注入 SecurityContext |
| 用户详情服务 | `UserDetailServiceImpl` | 实现 `UserDetailsService`，按用户名加载用户+角色+权限 |
| 安全配置 | `SecurityConfig` | 定义 URL 拦截规则、角色层级、密码编码器 |
| JWT 工具 | `JwtUtil` | Token 生成、解析、校验 |
| JWT Token 服务 | `JwtTokenService` | Redis 中 Token 的存取、注销、黑名单校验、过期清理 |
| Redis 配置 | `RedisConfig` | Redis 序列化配置、连接工厂 |
| 权限表达式 | `@PreAuthorize` | 方法级权限控制 |

#### 3.4.2 SecurityConfig 概要

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 开启方法级权限注解
public class SecurityConfig {

    // 1. 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 角色层级：SCHOOL > COLLEGE > COUNSELOR > PARENT
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
            "ROLE_SCHOOL > ROLE_COLLEGE\n" +
            "ROLE_COLLEGE > ROLE_COUNSELOR\n" +
            "ROLE_COUNSELOR > ROLE_PARENT"
        );
    }

    // 3. SecurityFilterChain
    //    - PC 端接口：/api/v1/** 需要认证
    //    - H5 家长端：/api/v1/parent/login 放行，其他 /api/v1/parent/** 需 PARENT 角色
    //    - 静态资源、Knife4j 文档放行
    //    - JwtAuthenticationFilter 插入 UsernamePasswordAuthenticationFilter 之前
    //    - 登出接口 /api/v1/auth/logout 需认证，调用 JwtTokenService 删除 Redis 中 Token

    // 4. RedisTemplate（用于 JWT Token 存储、缓存等）
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

#### 3.4.3 方法级权限示例

```java
// 学院管理：仅学校用户
@PreAuthorize("hasRole('SCHOOL')")
@PostMapping("/api/v1/colleges")
public Result<Void> createCollege() { ... }

// 学生管理：学校、学院、辅导员均可（具体数据范围在 Service 层控制）
@PreAuthorize("hasAnyRole('SCHOOL', 'COLLEGE', 'COUNSELOR')")
@GetMapping("/api/v1/students")
public Result<PageResult<StudentVO>> listStudents() { ... }

// 信件发送：仅辅导员
@PreAuthorize("hasRole('COUNSELOR')")
@PostMapping("/api/v1/letters/send")
public Result<SendResult> sendLetters() { ... }

// 家长端：仅家长角色
@PreAuthorize("hasRole('PARENT')")
@GetMapping("/api/v1/parent/letter")
public Result<LetterVO> getLetter() { ... }
```

#### 3.4.4 数据权限控制（Service 层）

```java
// 辅导员只能查看本班学生
// 学院用户只能查看本院学生
// 学校用户可查看全部

public PageResult<StudentVO> listStudents(StudentQueryDTO query) {
    Long currentUserId = SecurityUtil.getCurrentUserId();
    String roleType = SecurityUtil.getCurrentUserRole();

    LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();

    if ("COUNSELOR".equals(roleType)) {
        // 辅导员：查自己所带班级的学生
        List<Long> classIds = classMapper.selectList(
            Wrappers.lambdaQuery(ClassInfo.class)
                .eq(ClassInfo::getCounselorId, SecurityUtil.getCurrentUserId())
        ).stream().map(ClassInfo::getId).toList();
        wrapper.in(Student::getClassId, classIds);
    } else if ("COLLEGE".equals(roleType)) {
        // 学院用户：查本院学生
        wrapper.eq(Student::getCollegeId, SecurityUtil.getCurrentUserCollegeId());
    }
    // SCHOOL：不加限制，查全部

    return studentMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
}
```

#### 3.4.5 认证流程

```
PC 端登录:
  1. POST /api/v1/auth/login { username, password }
  2. AuthenticationManager.authenticate()
  3. UserDetailServiceImpl.loadUserByUsername() → 查询 sys_user + sys_role
  4. 校验 BCrypt 密码
  5. 生成 JWT（包含 userId, username, role 到 claims）
  6. 将 Token 存入 Redis，Key = "jwt:token:{userId}:{tokenId}"，TTL = 7 天
  7. 返回 Token + 用户信息

H5 家长端登录:
  1. POST /api/v1/parent/login { idCard, password }
  2. 按身份证号查 student → 查 parent_account
  3. 校验 BCrypt 密码
  4. 生成 JWT（包含 studentId, parentAccountId, role=PARENT）
  5. 将 Token 存入 Redis，Key = "jwt:token:parent:{parentAccountId}:{tokenId}"
  6. 返回 Token + 学生信息

后续请求:
  1. JwtAuthenticationFilter 从 Header 提取 Token
  2. 解析 JWT，获取 tokenId、userId 等信息
  3. 从 Redis 校验 Token 是否存在（Key = "jwt:token:{userId}:{tokenId}"）
     - 不存在 → 返回 401（Token 已注销或过期）
     - 存在 → 继续
  4. 构造 UsernamePasswordAuthenticationToken
  5. 注入 SecurityContextHolder
  6. 后续 @PreAuthorize 注解自动校验角色

主动注销:
  1. POST /api/v1/auth/logout（需携带当前 Token）
  2. JwtTokenService 从 Redis 删除对应 Token Key
  3. 该 Token 不再可用，后续请求返回 401

管理员强制下线:
  1. DELETE /api/v1/auth/token/{userId}
  2. JwtTokenService 从 Redis 删除该用户所有 Token Key（SCAN "jwt:token:{userId}:*" 后批量删除）
  3. 该用户所有 Token 立即失效
```

---

## 4. 功能需求

### 4.1 功能模块总览

```
家校互通系统
├── PC 管理后台
│   ├── 认证
│   │   ├── 登录/登出
│   │   └── 修改密码
│   ├── 基础数据管理
│   │   ├── 学院管理（学校用户）
│   │   ├── 班级管理
│   │   ├── 学生信息管理
│   │   ├── 家长信息管理
│   │   ├── 成绩录入与管理
│   │   └── 获奖与班干部管理
│   ├── 外部链接管理（学校用户）
│   │   ├── 链接列表
│   │   ├── 新增/编辑/删除链接
│   │   └── 链接排序
│   ├── 系统配置管理（学校用户）
│   │   └── 背景图/Logo/H5地址配置
│   ├── 信件模板管理
│   │   ├── 创建/编辑/删除模板
│   │   ├── 共享/复制模板
│   │   └── 模板列表查看
│   ├── 信件编辑与发送
│   │   ├── 套用模板生成信件
│   │   ├── 个性化修改信件内容
│   │   ├── 选择发送对象（按班级）
│   │   ├── 批量发送
│   │   ├── 重新发送（未读家长）
│   │   └── 发送状态查看
│   ├── 短信管理（学校用户）
│   │   └── 短信发送记录查看
│   └── 统计查询
│       ├── 发送统计（三级）
│       ├── 查阅统计（三级）
│       └── 数据导出（Excel）
├── H5 家长端
│   ├── 登录（身份证号 + 密码）
│   ├── 查看信件
│   ├── 提交反馈
│   └── 修改密码
```

### 4.2 核心功能详细说明

#### F001 - 用户登录

- **功能描述**：PC 端通过用户名密码登录；H5 端通过学生身份证号+密码登录
- **使用者**：所有角色
- **前置条件**：PC 端账号已由管理员创建；H5 端家长已随信件发送自动创建账号
- **基本流程**：输入凭证 → Spring Security 校验 → 返回 JWT Token → 前端跳转
- **异常流程**：密码错误 5 次锁定账号 30 分钟
- **业务规则**：
  - JWT Token 有效期 7 天，Claims 含 userId、username、role、collegeId
  - 同一账号多处登录，后者不会踢掉前者（Token 并存于 Redis 中）
- 支持主动注销：删除 Redis 中当前 Token，后续请求返回 401
- 管理员可强制下线指定用户（批量删除该用户所有 Token）
  - H5 首次登录强制跳转修改密码页面

#### F002 - 信件模板管理

- **功能描述**：创建和管理信件内容模板，支持富文本编辑，支持三级模板继承
- **使用者**：学校用户、学院用户、辅导员
- **业务规则**：
  - 模板内容为 HTML 富文本
  - 模板支持背景图、校徽/院徽设置
  - 模板共享采用复制模式：上级共享后，下级复制到自己的模板库，修改互不影响
  - 模板层级：学校模板 → 学院模板 → 辅导员模板
  - 支持同级辅导员之间相互共享模板

#### F003 - 信件批量发送

- **功能描述**：辅导员选择模板、编辑内容、选择学生 → 批量生成信件 → 模拟发送短信
- **使用者**：辅导员
- **前置条件**：模板已就绪、家长联系信息完整
- **业务规则**：
  - 按班级批量选择学生
  - 批量套用模板后可单独修改个别学生内容
  - 默认发送对象为父亲（is_default=1），若无联系方式依次取母亲、其他
  - 家长联系方式为空的学生标记为"发送失败"
  - 短信内容固定格式：`{称呼}家长您好：请查看学生在校情况：{链接}，账号为学生身份证号，密码为身份证后六位`
  - 发送时生成信件快照（复制模板内容到 student_letter），模板修改不影响已发送信件
  - 每个学生仅有一条发送记录（重复发送时更新）

#### F004 - 家长查看与反馈

- **功能描述**：家长通过短信链接在 H5 端登录查看信件，可选提交反馈
- **使用者**：家长
- **前置条件**：收到短信（含链接），知道学生身份证号
- **基本流程**：打开链接 → 登录 → 查看信件 → 自动标记已读 → （可选）提交反馈
- **业务规则**：
  - 初始密码为身份证后六位，首次登录后提示修改
  - 首次查看时自动更新 `status='READ'` 和 `read_time=now()`
  - 反馈支持文字 + 图片，为可选操作
  - 图片上传限制：jpg/png/gif，每张 ≤ 5MB

---

## 5. 业务流程

### 5.1 信件发送完整流程

```
0. 学校创建模板（可设背景图、校徽）
      ↓
  学院复制学校模板 → 修改为本院版本（设院徽）
      ↓
1. 辅导员选择模板 → 加载模板内容到富文本编辑器
2. 辅导员编辑信件内容（插入 ${score}、${award}、${cadre} 等占位符）
3. 辅导员选择发送班级 → 系统加载该班学生列表
4. 批量生成信件快照到选中的学生
5. 辅导员可对个别学生单独修改信件内容
6. 点击发送 → 后端遍历学生列表：
   ├── 查询 student_parent WHERE student_id = ? AND is_default = 1
   ├── 默认家长为父亲，无联系方式取母亲，都无取其他
   ├── 所有家长联系方式均为空 → 标记失败
   ├── 替换占位符（成绩、获奖、班干部等）
   ├── 拼接最终信件 HTML
   ├── INSERT student_letter（信件快照）
   ├── INSERT sms_record（模拟短信发送）
   └── 首次发送时 INSERT parent_account（username=身份证号，密码=身份证后六位）
7. 返回发送结果汇总（成功数 / 失败数 / 失败原因）
```

### 5.2 家长端流程

```
1. 家长收到短信 → 点击链接打开 H5 页面
2. 输入学生身份证号 + 密码登录
   ├── 首次登录 → 提示修改密码
   └── 已修改过 → 进入信件页面
3. 查看信件内容 → 系统自动更新 status='READ', read_time=now()
4. 可选：提交反馈（文字 + 图片）→ 保存到 parent_feedback 后结束
```

### 5.3 模板共享流转

```
学校用户创建模板 → 设为共享 → 学院用户可复制
学院用户创建模板 → 设为共享 → 本院辅导员可复制
辅导员A创建模板 → 设为共享 → 辅导员B可复制
复制后的模板独立，source_template_id 记录来源，修改不影响原模板
```

---

## 6. 功能逻辑分析 [重点]

### 6.1 信件批量发送逻辑

#### 6.1.1 完整执行路径

```
1. POST /api/v1/letters/send → LetterController.send()
   @PreAuthorize("hasRole('COUNSELOR')")
2. @Valid 校验请求参数（studentIds 非空, templateId 有效）
3. LetterServiceImpl.sendLetters():
   a. 查询模板内容
   b. 遍历 studentIds:
      - 查询学生信息（校验是否本班学生）
      - 查询默认家长联系方式
      - 无联系方式 → 记录失败，跳过
      - 有联系方式 → 替换占位符，拼接 HTML
      - INSERT / UPDATE student_letter（快照内容，按 student_id UNIQUE）
      - 模拟发送短信 → INSERT sms_record
      - 首次发送 → INSERT parent_account（username=身份证号，password=身份证后六位）
   c. 返回汇总结果
```

#### 6.1.2 关键判断逻辑

```
FOR EACH studentId IN request.studentIds:
    student = studentMapper.selectById(studentId)

    // 辅导员数据权限：只能发本班学生
    IF student.class.counselorId != currentUserId THEN
        CONTINUE  // 跳过非本班学生
    END IF

    parent = parentMapper.selectOne(
        Wrappers.lambdaQuery(StudentParent.class)
            .eq(StudentParent::getStudentId, studentId)
            .eq(StudentParent::getIsDefault, true)
    )

    // 若默认家长无联系方式，依次尝试其他家长
    IF parent IS NULL OR parent.phone IS NULL THEN
        parent = findFirstParentWithPhone(studentId)
    END IF

    IF parent IS NULL OR parent.phone IS NULL THEN
        failList.add({studentId, student.name, "联系方式为空"})
        CONTINUE
    END IF

    // 拼接信件内容（模板 + 占位符替换 + 个性化修改）
    content = template.content
                .replace("${score}", buildScoreTable(studentId))
                .replace("${award}", buildAwardInfo(studentId))
                .replace("${cadre}", buildCadreInfo(studentId))
    IF request.customContents.containsKey(studentId) THEN
        content = request.customContents.get(studentId)
    END IF

    // 写入信件记录（快照）
    letter = new StudentLetter()
    letter.studentId = studentId
    letter.parentId = parent.id
    letter.templateId = templateId
    letter.content = content
    letter.status = "SENT"
    studentLetterMapper.insert(letter)

    // 模拟短信发送
    sms = new SmsRecord()
    sms.studentId = studentId
    sms.parentId = parent.id
    sms.phone = parent.phone
    sms.content = buildSmsContent(parent, student)
    sms.status = simulateSend()  // 本地模拟，返回 SUCCESS/FAIL
    smsRecordMapper.insert(sms)

    // 首次发送创建家长登录账号
    IF NOT EXISTS parent_account WHERE student_id = studentId THEN
        rawPassword = student.idCard.substring(student.idCard.length() - 6)
        account = new ParentAccount()
        account.studentId = studentId
        account.username = student.idCard
        account.password = BCrypt.encode(rawPassword)
        parentAccountMapper.insert(account)
    END IF
END FOR

RETURN { totalCount, successCount, failCount, failList }
```

#### 6.1.3 涉及的数据表

| 表名 | 操作 | 说明 |
|------|------|------|
| student | SELECT | 获取学生信息 |
| student_parent | SELECT | 获取发送对象联系方式 |
| parent_account | SELECT / INSERT | 检查/创建家长登录账号 |
| letter_template | SELECT | 获取模板内容 |
| student_score | SELECT | 查询成绩用于占位符替换 |
| student_award | SELECT | 查询获奖用于占位符替换 |
| student_cadre | SELECT | 查询班干部用于占位符替换 |
| student_letter | INSERT / UPDATE | 写入信件快照 |
| sms_record | INSERT | 写入短信发送记录 |

#### 6.1.4 异常处理

| 异常场景 | 触发条件 | 处理方式 | 返回信息 |
|----------|----------|----------|----------|
| 无联系方式 | 学生所有家长 phone 为空 | 标记失败，跳过 | "联系方式为空" |
| 模板不存在 | 模板已被删除 | 提示重新选择 | "模板不存在" |
| 班级无学生 | 选择班级下无在校学生 | 提示确认 | "该班级下无在校学生" |
| 重复发送 | 该学生已有发送记录 | 更新已有记录 | 正常 |
| 非本班学生 | 辅导员选择了他班学生 | 跳过 | 静默跳过 |

### 6.2 统计计算逻辑

```
一级统计（学校用户）:
  @PreAuthorize("hasRole('SCHOOL')")
  应发人数 = COUNT(student WHERE status='在校')
  实发人数 = COUNT(DISTINCT sms_record.student_id WHERE status = 'SUCCESS')
  完成率 = 实发人数 / 应发人数 * 100%
  按学院分组

  > 说明：实发人数以 sms_record 表中 status='SUCCESS' 为准，而非 student_letter。
  > 原因：student_letter 记录的是信件生成（快照），即使信件生成成功，短信发送可能失败。
  > 家长实际收到短信才能算"已发送"，因此统计口径以短信送达为准。

二级统计（学院用户）:
  @PreAuthorize("hasAnyRole('SCHOOL', 'COLLEGE')")
  同一级公式，按辅导员/班级维度
  过滤 college_id = 当前用户学院

三级统计（辅导员）:
  @PreAuthorize("hasAnyRole('SCHOOL', 'COLLEGE', 'COUNSELOR')")
  列出本班每个学生的发送状态（成功/失败/未发送）

查阅统计:
  已查阅 = COUNT(student_letter WHERE status='READ')
  查阅率 = 已查阅 / 实发人数 * 100%

反馈统计:
  反馈人数 = COUNT(DISTINCT parent_feedback.student_id)
  支持导出
```

---

## 7. 数据库设计 [重点]

### 7.1 ER 概要

```
sys_role (1) ----> (N) sys_user
college (1) ----> (N) sys_user
college (1) ----> (N) class_info
college (1) ----> (N) student
class_info (1) ----> (N) student
sys_user(counselor) (1) ----> (N) student
student (1) ----> (N) student_parent
student (1) ----> (1) parent_account
student (1) ----> (N) student_score
student (1) ----> (N) student_award
student (1) ----> (N) student_cadre
sys_user (1) ----> (N) letter_template
student (1) ----> (1) student_letter
student_parent (1) ----> (N) student_letter
letter_template (1) ----> (N) student_letter
student_letter (1) ----> (N) parent_feedback
student (1) ----> (N) sms_record
student_parent (1) ----> (N) sms_record
```

### 7.2 表结构设计

#### 表1：sys_role（系统角色）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| role_name | VARCHAR | 30 | 是 | - | 角色名称 |
| role_code | VARCHAR | 30 | 是 | - | 角色编码：ROLE_SCHOOL / ROLE_COLLEGE / ROLE_COUNSELOR / ROLE_PARENT |
| description | VARCHAR | 100 | 否 | NULL | 角色描述 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

> 初始数据：4 条固定角色记录。角色由系统预置，不提供前端增删接口。

#### 表2：college（学院）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| name | VARCHAR | 100 | 是 | - | 学院名称 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | 逻辑删除 |

#### 表3：sys_user（系统用户）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| username | VARCHAR | 50 | 是 | - | 登录用户名，UNIQUE |
| password | VARCHAR | 200 | 是 | - | BCrypt 加密 |
| real_name | VARCHAR | 50 | 是 | - | 真实姓名 |
| role_id | BIGINT | - | 是 | - | 关联 sys_role.id |
| college_id | BIGINT | - | 否 | NULL | 所属学院ID（学院用户/辅导员必填，学校用户 NULL） |
| phone | VARCHAR | 20 | 否 | NULL | 联系电话 |
| status | TINYINT | - | 是 | 1 | 1-正常 0-禁用 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 查询用户时 JOIN sys_role 获取角色编码，用于 Spring Security 权限判断。

#### 表4：class_info（班级）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| name | VARCHAR | 100 | 是 | - | 班级名称（如"计算机2101"） |
| college_id | BIGINT | - | 是 | - | 所属学院ID |
| counselor_id | BIGINT | - | 否 | NULL | 辅导员用户ID（sys_user.id） |
| grade | VARCHAR | 10 | 是 | - | 年级（如"2021"） |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

#### 表5：student（学生）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_no | VARCHAR | 30 | 是 | - | 学号，UNIQUE |
| name | VARCHAR | 50 | 是 | - | 姓名 |
| id_card | VARCHAR | 18 | 是 | - | 身份证号，UNIQUE |
| college_id | BIGINT | - | 是 | - | 学院ID |
| class_id | BIGINT | - | 是 | - | 班级ID |
| counselor_id | BIGINT | - | 是 | - | 辅导员用户ID |
| enrollment_year | VARCHAR | 4 | 是 | - | 入学年份 |
| status | VARCHAR | 10 | 是 | '在校' | 在校 / 休学 / 毕业 / 退学 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 说明：`counselor_id` 冗余存储，用于辅导员数据权限快速过滤。与 `class_info.counselor_id` 保持一致。

#### 表6：student_parent（学生家长）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| name | VARCHAR | 50 | 否 | NULL | 家长姓名 |
| relation | VARCHAR | 20 | 是 | - | FATHER / MOTHER / GRANDMA / GRANDPA / OTHER |
| phone | VARCHAR | 20 | 否 | NULL | 联系电话 |
| is_default | TINYINT(1) | - | 是 | 0 | 是否默认发送对象 |
| source_type | VARCHAR | 20 | 是 | 'MANUAL' | MANUAL / IMPORT / SMS |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 说明：家长登录账号独立存储于 `parent_account` 表，`student_parent` 表仅存储家长联系信息。`source_type` 区分数据来源：MANUAL（手动录入）、IMPORT（Excel导入）、SMS（短信创建）。

#### 表7：parent_account（家长登录账号）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID，UNIQUE（一个学生一个账号） |
| username | VARCHAR | 18 | 是 | - | 登录用户名（学生身份证号），UNIQUE |
| password | VARCHAR | 200 | 是 | - | BCrypt 加密 |
| last_login_time | DATETIME | - | 否 | NULL | 最后登录时间 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

> 说明：家长账号与学生一对一绑定。首次发送信件时自动创建，初始密码为学生身份证后六位（BCrypt 加密）。独立存储避免因默认联系人切换导致密码归属混乱。

#### 表8：student_score（学生成绩）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| course_name | VARCHAR | 100 | 是 | - | 课程名称 |
| score | DECIMAL(5,1) | - | 否 | NULL | 分数 |
| academic_year | VARCHAR | 10 | 是 | - | 学年（如"2025-2026"） |
| semester | TINYINT | - | 是 | - | 学期 1/2 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

#### 表9：student_award（学生获奖）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| award_name | VARCHAR | 200 | 是 | - | 奖项名称 |
| award_level | VARCHAR | 50 | 否 | NULL | 奖项级别（国家级/省级/校级/院级） |
| award_time | DATETIME | - | 否 | NULL | 获奖时间 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 说明：用于信件 `${award}` 占位符的数据来源。辅导员录入或批量导入。

#### 表10：student_cadre（学生班干部）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| position_name | VARCHAR | 100 | 是 | - | 职务名称（如"班长"、"学习委员"） |
| start_time | DATETIME | - | 否 | NULL | 任职开始时间 |
| end_time | DATETIME | - | 否 | NULL | 任职结束时间 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 说明：用于信件 `${cadre}` 占位符的数据来源。辅导员录入或批量导入。

#### 表11：letter_template（信件模板）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| name | VARCHAR | 100 | 是 | - | 模板名称 |
| content | TEXT | - | 是 | - | HTML 富文本内容 |
| background_url | VARCHAR | 500 | 否 | NULL | 背景图路径 |
| logo_url | VARCHAR | 300 | 否 | NULL | 校徽/院徽图片路径 |
| creator_id | BIGINT | - | 是 | - | 创建者用户ID |
| creator_type | VARCHAR | 20 | 是 | - | SCHOOL / COLLEGE / COUNSELOR |
| college_id | BIGINT | - | 否 | NULL | 所属学院（学院/辅导员创建时记录） |
| is_shared | TINYINT(1) | - | 是 | 0 | 是否共享 |
| source_template_id | BIGINT | - | 否 | NULL | 复制来源模板ID |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

#### 表12：student_letter（学生信件）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| parent_id | BIGINT | - | 是 | - | 发送给哪个家长 |
| template_id | BIGINT | - | 否 | NULL | 使用的模板ID |
| content | MEDIUMTEXT | - | 是 | - | 最终发送的 HTML 内容（快照） |
| status | VARCHAR | 20 | 是 | 'UNSEND' | UNSEND / SENT / READ |
| read_time | DATETIME | - | 否 | NULL | 家长查阅时间 |
| send_time | DATETIME | - | 否 | NULL | 发送时间 |
| counselor_id | BIGINT | - | 是 | - | 发送的辅导员ID |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| is_deleted | TINYINT(1) | - | 是 | 0 | |

> 说明：发送时生成信件快照（复制模板 content 到本表），模板修改不影响已发送信件。`status` 使用 UNSEND（未发送）/ SENT（已发送）/ READ（已查阅）。**每个学生仅保留一条记录**，重复发送时更新已有记录（`student_id` 上加 UNIQUE 约束）。

#### 表13：sms_record（短信发送记录）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| student_id | BIGINT | - | 是 | - | 学生ID |
| parent_id | BIGINT | - | 是 | - | 家长ID |
| phone | VARCHAR | 20 | 是 | - | 发送号码 |
| content | VARCHAR | 500 | 是 | - | 短信内容 |
| status | VARCHAR | 20 | 是 | 'PENDING' | PENDING / SUCCESS / FAIL |
| fail_reason | VARCHAR | 200 | 否 | NULL | 失败原因 |
| send_time | DATETIME | - | 否 | NULL | 发送时间 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

> 说明：短信为本地模拟发送，不调用真实短信接口。`status` 模拟随机结果，大部分返回 SUCCESS。

#### 表14：parent_feedback（家长反馈）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| letter_id | BIGINT | - | 是 | - | 关联 student_letter.id |
| student_id | BIGINT | - | 是 | - | 学生ID（冗余，方便统计） |
| parent_id | BIGINT | - | 是 | - | 家长ID（冗余） |
| content | TEXT | - | 是 | - | 反馈文字内容 |
| images | VARCHAR | 1000 | 否 | NULL | 图片路径，多张用逗号分隔 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

#### 表15：external_link（外部链接）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| title | VARCHAR | 100 | 是 | - | 链接标题 |
| url | VARCHAR | 500 | 是 | - | 链接地址 |
| sort | INT | - | 是 | 0 | 排序序号（越小越靠前） |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

> 说明：学校用户管理的外部链接，用于 H5 家长端展示相关资源入口。

#### 表16：system_config（系统配置）

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|------|------|--------|------|
| id | BIGINT | - | 是 | AUTO | 主键 |
| config_key | VARCHAR | 100 | 是 | - | 配置键，UNIQUE |
| config_value | VARCHAR | 500 | 是 | - | 配置值 |
| description | VARCHAR | 200 | 否 | NULL | 配置说明 |
| create_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |
| update_time | DATETIME | - | 是 | CURRENT_TIMESTAMP | |

> 说明：系统级配置项，以 key-value 形式存储。典型配置项：
> - `LETTER_BACKGROUND`：信件默认背景图路径
> - `SCHOOL_LOGO`：学校 Logo 路径
> - `H5_BASE_URL`：H5 端访问地址（用于短信中拼接链接）
> 学校用户可修改配置，无需改模板即可切换背景风格。

### 7.3 索引设计

| 索引名 | 表名 | 字段 | 类型 | 说明 |
|--------|------|------|------|------|
| uk_sys_user_username | sys_user | username | UNIQUE | 用户名唯一 |
| idx_sys_user_role | sys_user | role_id | BTREE | 按角色查用户 |
| idx_sys_user_college | sys_user | college_id | BTREE | 按学院查用户 |
| idx_class_college | class_info | college_id | BTREE | 按学院查班级 |
| idx_class_counselor | class_info | counselor_id | BTREE | 辅导员查班级 |
| idx_student_class | student | class_id | BTREE | 按班级查学生 |
| idx_student_college | student | college_id | BTREE | 按学院查学生 |
| idx_student_counselor | student | counselor_id | BTREE | 辅导员查学生 |
| uk_student_no | student | student_no | UNIQUE | 学号唯一 |
| uk_student_id_card | student | id_card | UNIQUE | 身份证号唯一 |
| idx_parent_student | student_parent | student_id | BTREE | 按学生查家长 |
| uk_parent_account_student | parent_account | student_id | UNIQUE | 一个学生一个账号 |
| uk_parent_account_username | parent_account | username | UNIQUE | 身份证号唯一 |
| idx_score_student | student_score | student_id | BTREE | 按学生查成绩 |
| idx_award_student | student_award | student_id | BTREE | 按学生查获奖 |
| idx_cadre_student | student_cadre | student_id | BTREE | 按学生查班干部 |
| idx_template_creator | letter_template | creator_id, creator_type | BTREE | 按创建者查模板 |
| uk_letter_student | student_letter | student_id | UNIQUE | 每个学生仅一条信件记录 |
| idx_letter_counselor | student_letter | counselor_id | BTREE | 按辅导员查信件 |
| idx_sms_student | sms_record | student_id | BTREE | 按学生查短信 |
| idx_feedback_letter | parent_feedback | letter_id | BTREE | 按信件查反馈 |
| idx_feedback_student | parent_feedback | student_id | BTREE | 按学生查反馈 |
| idx_link_sort | external_link | sort | BTREE | 按排序查链接 |
| uk_config_key | system_config | config_key | UNIQUE | 配置键唯一 |

### 7.4 数据字典

| 表名.字段 | 值 | 说明 |
|-----------|-----|------|
| sys_role.role_code | ROLE_SCHOOL | 学校用户 |
| sys_role.role_code | ROLE_COLLEGE | 学院用户 |
| sys_role.role_code | ROLE_COUNSELOR | 辅导员 |
| sys_role.role_code | ROLE_PARENT | 家长 |
| sys_user.status | 1 | 正常 |
| sys_user.status | 0 | 禁用 |
| student.status | 在校 / 休学 / 毕业 / 退学 | |
| student_parent.relation | FATHER / MOTHER / GRANDMA / GRANDPA / OTHER | |
| student_parent.source_type | MANUAL / IMPORT / SMS | |
| student_award.award_level | 国家级 / 省级 / 校级 / 院级 | |
| letter_template.creator_type | SCHOOL / COLLEGE / COUNSELOR | |
| student_letter.status | UNSEND / SENT / READ | 未发送 / 已发送 / 已查阅 |
| sms_record.status | PENDING / SUCCESS / FAIL | |
| parent_feedback.images | 逗号分隔图片路径 | |
| system_config.config_key | LETTER_BACKGROUND / SCHOOL_LOGO / H5_BASE_URL | 系统配置键 |

---

## 8. 接口设计 [重点]

### 8.1 接口规范

- **Base URL**：`http://localhost:8080/api/v1`
- **家长端 Base URL**：`http://localhost:8080/api/v1/parent`
- **请求格式**：JSON（Content-Type: application/json）
- **响应格式**：统一响应体
- **认证方式**：JWT Token（Header: `Authorization: Bearer {token}`）
- **权限控制**：Spring Security `@PreAuthorize` 方法级注解
- **分页参数**：`page`（页码，默认1）、`size`（每页数，默认10）

### 8.2 统一响应体

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1717425600000
}
```

### 8.3 状态码规范

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 过期 |
| 403 | 无权限（角色不匹配 / 数据越权） |
| 404 | 资源不存在 |
| 409 | 数据冲突（如学号重复） |
| 500 | 服务器内部错误 |

### 8.4 认证模块

#### PC 端登录

```
POST /api/v1/auth/login
```

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例**：
```json
{
  "username": "counselor01",
  "password": "123456"
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "counselor01",
      "realName": "张老师",
      "roleCode": "ROLE_COUNSELOR",
      "collegeId": 1,
      "collegeName": "计算机学院"
    }
  }
}
```

#### PC 端注销

```
POST /api/v1/auth/logout
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | - | - | 无需请求体，从 Header 提取 Token |

**成功响应**：
```json
{
  "code": 200,
  "message": "注销成功",
  "data": null
}
```

> 后端从 Redis 中删除当前 Token，后续该 Token 无法使用。

#### 管理员强制下线

```
DELETE /api/v1/auth/token/{userId}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 路径参数，目标用户ID |

**权限**：`ROLE_SCHOOL`

**成功响应**：
```json
{
  "code": 200,
  "message": "已强制下线该用户的所有会话",
  "data": { "revokedCount": 3 }
}
```

#### H5 家长端登录

```
POST /api/v1/parent/login
```

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| idCard | String | 是 | 学生身份证号 |
| password | String | 是 | 密码 |

### 8.5 基础数据模块

> 所有接口除特别标注外，均需携带 JWT Token。权限为 `@PreAuthorize` 注解的角色要求，数据范围在 Service 层根据 `college_id` / `counselor_id` 过滤。

#### 学院管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 学院列表 | GET | /api/v1/colleges | 是（所有PC角色） |
| 2 | 新增学院 | POST | /api/v1/colleges | SCHOOL |
| 3 | 编辑学院 | PUT | /api/v1/colleges/{id} | SCHOOL |
| 4 | 删除学院 | DELETE | /api/v1/colleges/{id} | SCHOOL |

#### 班级管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 班级列表 | GET | /api/v1/classes | 是 |
| 2 | 新增班级 | POST | /api/v1/classes | SCHOOL / COLLEGE |
| 3 | 编辑班级 | PUT | /api/v1/classes/{id} | SCHOOL / COLLEGE |
| 4 | 删除班级 | DELETE | /api/v1/classes/{id} | SCHOOL / COLLEGE |

#### 学生管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 学生分页列表 | GET | /api/v1/students | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 学生详情 | GET | /api/v1/students/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 3 | 新增学生 | POST | /api/v1/students | SCHOOL / COLLEGE / COUNSELOR |
| 4 | 编辑学生 | PUT | /api/v1/students/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 5 | 删除学生 | DELETE | /api/v1/students/{id} | SCHOOL / COLLEGE |
| 6 | 批量导入 | POST | /api/v1/students/import | SCHOOL / COLLEGE |
| 7 | 导出学生列表 | GET | /api/v1/students/export | SCHOOL / COLLEGE |

**GET /api/v1/students 查询参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数，默认10 |
| classId | Long | 否 | 班级ID |
| keyword | String | 否 | 姓名/学号搜索 |
| status | String | 否 | 学籍状态筛选 |

#### 家长信息管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 学生家长列表 | GET | /api/v1/students/{studentId}/parents | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 添加家长 | POST | /api/v1/students/{studentId}/parents | SCHOOL / COLLEGE / COUNSELOR |
| 3 | 编辑家长 | PUT | /api/v1/parents/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 4 | 删除家长 | DELETE | /api/v1/parents/{id} | SCHOOL / COLLEGE |
| 5 | 批量设置默认发送对象 | PUT | /api/v1/parents/batch-default | COUNSELOR |

#### 成绩管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 学生成绩列表 | GET | /api/v1/students/{studentId}/scores | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 新增成绩 | POST | /api/v1/students/{studentId}/scores | COUNSELOR |
| 3 | 批量导入成绩 | POST | /api/v1/scores/import | COUNSELOR |
| 4 | 编辑成绩 | PUT | /api/v1/scores/{id} | COUNSELOR |
| 5 | 删除成绩 | DELETE | /api/v1/scores/{id} | COUNSELOR |

#### 外部链接管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 链接列表 | GET | /api/v1/links | SCHOOL |
| 2 | 新增链接 | POST | /api/v1/links | SCHOOL |
| 3 | 编辑链接 | PUT | /api/v1/links/{id} | SCHOOL |
| 4 | 删除链接 | DELETE | /api/v1/links/{id} | SCHOOL |
| 5 | 更新排序 | PUT | /api/v1/links/sort | SCHOOL |

#### 获奖与班干部管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 学生获奖列表 | GET | /api/v1/students/{studentId}/awards | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 新增获奖 | POST | /api/v1/students/{studentId}/awards | COUNSELOR |
| 3 | 删除获奖 | DELETE | /api/v1/awards/{id} | COUNSELOR |
| 4 | 学生班干部列表 | GET | /api/v1/students/{studentId}/cadres | SCHOOL / COLLEGE / COUNSELOR |
| 5 | 新增班干部 | POST | /api/v1/students/{studentId}/cadres | COUNSELOR |
| 6 | 删除班干部 | DELETE | /api/v1/cadres/{id} | COUNSELOR |

#### 系统配置管理

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 配置列表 | GET | /api/v1/configs | SCHOOL |
| 2 | 更新配置 | PUT | /api/v1/configs/{key} | SCHOOL |

### 8.6 模板管理模块

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 我的模板列表 | GET | /api/v1/templates | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 共享模板列表 | GET | /api/v1/templates/shared | SCHOOL / COLLEGE / COUNSELOR |
| 3 | 模板详情 | GET | /api/v1/templates/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 4 | 创建模板 | POST | /api/v1/templates | SCHOOL / COLLEGE / COUNSELOR |
| 5 | 编辑模板 | PUT | /api/v1/templates/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 6 | 删除模板 | DELETE | /api/v1/templates/{id} | SCHOOL / COLLEGE / COUNSELOR |
| 7 | 共享/取消共享 | PUT | /api/v1/templates/{id}/share | SCHOOL / COLLEGE / COUNSELOR |
| 8 | 复制模板 | POST | /api/v1/templates/{id}/copy | SCHOOL / COLLEGE / COUNSELOR |

> 模板数据权限：用户只能编辑/删除自己创建的模板；共享模板可查看不可修改；复制后生成新模板，归属复制者。

### 8.7 信件发送模块

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 发送列表 | GET | /api/v1/letters | SCHOOL / COLLEGE / COUNSELOR |
| 2 | 批量发送 | POST | /api/v1/letters/send | COUNSELOR |
| 3 | 重新发送 | POST | /api/v1/letters/resend | COUNSELOR |
| 4 | 修改单个信件 | PUT | /api/v1/letters/{id} | COUNSELOR |

**POST /api/v1/letters/send 请求体**：
```json
{
  "studentIds": [1, 2, 3, 4, 5],
  "templateId": 1,
  "customContents": {
    "3": "<h1>针对学生3的个性化内容</h1>"
  }
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "发送完成",
  "data": {
    "totalCount": 5,
    "successCount": 4,
    "failCount": 1,
    "failList": [
      {
        "studentId": 3,
        "studentName": "王五",
        "reason": "联系方式为空"
      }
    ]
  }
}
```

### 8.8 统计模块

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 发送统计（学校维度） | GET | /api/v1/statistics/send/school | SCHOOL |
| 2 | 发送统计（学院维度） | GET | /api/v1/statistics/send/college | SCHOOL / COLLEGE |
| 3 | 发送统计（班级维度） | GET | /api/v1/statistics/send/class | SCHOOL / COLLEGE / COUNSELOR |
| 4 | 查阅统计 | GET | /api/v1/statistics/read | SCHOOL / COLLEGE / COUNSELOR |
| 5 | 反馈统计 | GET | /api/v1/statistics/feedback | SCHOOL / COLLEGE / COUNSELOR |
| 6 | 未发送名单 | GET | /api/v1/statistics/unsent | SCHOOL / COLLEGE / COUNSELOR |
| 7 | 未查阅名单 | GET | /api/v1/statistics/unread | SCHOOL / COLLEGE / COUNSELOR |
| 8 | 导出发送统计 | GET | /api/v1/statistics/send/export | SCHOOL / COLLEGE |
| 9 | 导出查阅统计 | GET | /api/v1/statistics/read/export | SCHOOL / COLLEGE |
| 10 | 导出反馈统计 | GET | /api/v1/statistics/feedback/export | SCHOOL / COLLEGE |

> 导出接口返回 Excel 文件（`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`），使用 EasyExcel 生成。

**GET /api/v1/statistics/send/school 响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "colleges": [
      {
        "collegeId": 1,
        "collegeName": "计算机学院",
        "shouldSendCount": 500,
        "actualSendCount": 480,
        "completionRate": 96.0
      }
    ],
    "summary": {
      "shouldSendCount": 2000,
      "actualSendCount": 1920,
      "completionRate": 96.0
    }
  }
}
```

### 8.9 短信管理模块

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 短信记录列表 | GET | /api/v1/sms-records | SCHOOL |
| 2 | 按学生查短信 | GET | /api/v1/sms-records/student/{studentId} | SCHOOL |

### 8.10 家长端 H5 接口

| 序号 | 接口名称 | 方法 | URL | 权限 |
|------|----------|------|-----|:--:|
| 1 | 登录 | POST | /api/v1/parent/login | 否 |
| 2 | 获取信件内容 | GET | /api/v1/parent/letter | PARENT |
| 3 | 获取学生成绩 | GET | /api/v1/parent/scores | PARENT |
| 4 | 提交反馈 | POST | /api/v1/parent/feedback | PARENT |
| 5 | 修改密码 | PUT | /api/v1/parent/password | PARENT |
| 6 | 获取外部链接 | GET | /api/v1/parent/links | PARENT |

**GET /api/v1/parent/letter 响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "letterId": 1,
    "studentName": "张三",
    "content": "<h1>安全告知书</h1>...",
    "sendTime": "2026-06-05 10:00:00",
    "collegeName": "计算机学院",
    "logoUrl": "/uploads/logo/school-logo.png",
    "backgroundUrl": "/uploads/bg/letter-bg.png",
    "status": "READ",
    "hasFeedback": false
  }
}
```

---

## 9. 非功能性需求

### 9.1 性能要求

| 指标 | 要求 |
|------|------|
| 接口响应时间 | 列表查询 < 300ms，批量发送 < 5s |
| 并发用户数 | < 50（实训级别） |
| 数据库连接池 | HikariCP，最大连接数 20 |

### 9.2 安全要求

- 密码使用 BCrypt 加密存储
- PC 端和 H5 端均使用 JWT 认证，Token 有效期 7 天，Redis 中 TTL 与 JWT 过期时间一致
- Token 存入 Redis 后支持主动注销和强制下线，解决无状态 JWT 无法主动失效的问题
- 每次请求 JwtAuthenticationFilter 校验 Redis 中 Token 是否存在，已注销的 Token 直接返回 401
- 登出时删除 Redis 中对应的 Token Key
- Spring Security `@EnableMethodSecurity` 开启方法级权限控制
- 数据权限在 Service 层根据角色过滤（college_id / counselor_id）
- SQL 参数使用 MyBatis-Plus 预编译，防止 SQL 注入
- 辅导员只能操作本班学生数据，通过角色 + 数据范围双重校验
- 富文本内容存储前进行 XSS 过滤
- 文件上传限制类型（jpg/png/gif）和大小（5MB）
- JWT Token 不含敏感信息，仅存 userId、username、roleCode、collegeId

### 9.3 日志要求

- 关键操作记录日志：登录、信件发送、模板修改、家长查阅、权限拒绝
- 使用 SLF4J + Logback，日志级别 INFO
- 403 权限拒绝记录 WARN 级别日志

---

## 10. 附录

### 10.1 关键依赖

```xml
<!-- pom.xml 核心依赖 -->
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.9</version>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Redis (JWT Token 存储) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Knife4j API文档 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>4.5.0</version>
    </dependency>

    <!-- EasyExcel（成绩批量导入） -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>easyexcel</artifactId>
        <version>4.0.3</version>
    </dependency>
</dependencies>
```

### 10.2 Redis 配置

#### application.yml Redis 连接配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: 3000ms
```

#### Redis Key 设计规范

| Key 模式 | 说明 | TTL |
|----------|------|-----|
| `jwt:token:{userId}:{tokenId}` | PC 端 JWT Token 存储 | 7 天 |
| `jwt:token:parent:{parentAccountId}:{tokenId}` | H5 家长端 JWT Token 存储 | 7 天 |
| `jwt:blacklist:{tokenId}` | Token 黑名单（手动吊销） | 永久 |

> `tokenId` 为 JWT 生成时的 UUID，存储在 JWT Claims 的 `jti` 字段中。

#### JwtTokenService 核心方法

```java
@Service
public class JwtTokenService {

    // 存储 Token 到 Redis
    public void storeToken(String keyPrefix, Long userId, String tokenId, String token, long ttlSeconds);

    // 校验 Token 是否有效（Redis 中存在且未在黑名单）
    public boolean isTokenValid(String userId, String tokenId);

    // 删除单个 Token（主动注销）
    public void revokeToken(String userId, String tokenId);

    // 强制下线用户所有 Token
    public int revokeAllUserTokens(String keyPrefix, Long userId);

    // 将 Token 加入黑名单
    public void addToBlacklist(String tokenId, long ttlSeconds);
}
```

### 10.3 待确认事项

| 序号 | 事项 | 当前处理方式 | 备注 |
|------|------|-------------|------|
| 1 | 短信平台对接 | 本地模拟（sms_record 表记录） | 后续可替换为真实短信服务（阿里云/腾讯云） |
| 2 | 成绩数据来源 | 自建表 + 辅导员录入/导入 | 后续可对接教务处系统 |
| 3 | 家校通链接地址 | 配置项（application.yml） | 部署时修改 |
| 4 | 角色是否可动态扩展 | 当前固定 4 种角色 | 实训阶段固定，后续可通过角色-权限表扩展 |
| 5 | 文件存储 | 本地 uploads/ 目录 | 后续可切换 MinIO / OSS |

### 10.4 推荐开发顺序

```
认证授权（Spring Security + JWT + Redis + RBAC）
    ↓
基础数据管理（学院/班级/学生/家长）
    ↓
模板管理（含共享/复制）
    ↓
信件发送（核心链路）
    ↓
家长查阅与反馈（H5）
    ↓
统计分析（三级）
    ↓
外部链接管理 + 短信记录查看
```

优先打通主业务链路（模板 → 发送 → 查阅），再逐步完善扩展功能。
