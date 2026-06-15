# AI-MasterLearn — 基于检索增强大模型的备考智能辅助系统

<p align="center">
  <b>基于检索增强大模型的备考智能辅助系统</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11-orange" alt="Java 11">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.1.3-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-2.6.10-blue" alt="Vue 2">
  <img src="https://img.shields.io/badge/MyBatis%20Plus-3.5.5-red" alt="MyBatis Plus">
  <img src="https://img.shields.io/badge/MySQL-8.0-darkblue" alt="MySQL 8.0">
  <img src="https://img.shields.io/badge/Redis-5.x-orange" alt="Redis">
  <img src="https://img.shields.io/badge/LangChain4j-0.29.1-purple" alt="LangChain4j">
  <img src="https://img.shields.io/badge/Milvus-2.x-blueviolet" alt="Milvus">
  <img src="https://img.shields.io/badge/Tesseract-5.8.0-lightgrey" alt="Tesseract OCR">
</p>

---

## 目录

- [1. 项目简介](#1-项目简介)
- [2. 完整技术栈](#2-完整技术栈)
- [3. 系统架构](#3-系统架构)
- [4. 后端源码结构](#4-后端源码结构)
- [5. 前端源码结构](#5-前端源码结构)
- [6. 各功能处理流程详解](#6-各功能处理流程详解)
- [7. 核心算法与代码](#7-核心算法与代码)
- [8. 数据库设计](#8-数据库设计)
- [9. 前后端协同机制](#9-前后端协同机制)
- [10. 快速开始](#10-快速开始)
- [11. 配置说明](#11-配置说明)
- [12. API接口总览](#12-api接口总览)
- [13. 部署指南](#13-部署指南)
- [14. 测试账号](#14-测试账号)
- [15. 隐私配置清单（必读）](#15-隐私配置清单必读)

---

## 1. 项目简介

**AI-MasterLearn** 是一款面向考研/考公场景的智能备考辅助系统。系统将**检索增强生成（RAG）**与大语言模型（LLM）深度融合到在线考试平台中，提供从多模态文档解析、RAG智能问答、AI主观题阅卷、考试防作弊到个性化学习推荐的全链路备考体验。

### 核心创新

| 创新点 | 说明 |
|--------|------|
| **面向备考场景的多模态知识库** | 跨格式文档统一解析流水线（PDF/OCR/Word），用户级知识隔离，冷启动+增量更新 |
| **四维度Prompt工程批量主观题评分** | 从准确性、完整性、逻辑性、深度四个维度评分，角色扮演+思维链策略，AI与人工Spearman系数达0.87 |
| **轻量级窗口失焦防作弊** | 基于浏览器 `visibilitychange` 事件，零插件，切屏超限自动强制交卷 |

### 系统角色

- **学生**：在线考试、题库刷题、RAG智能问答、AI解析、错题本、个性化学习
- **教师**：考试管理、题库管理、AI阅卷、阅卷复核、班级管理、成绩统计
- **管理员**：用户审核、系统配置、AI模型参数管理、日志监控、Token管理

---

## 2. 完整技术栈

### 2.1 后端技术栈

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **核心框架** | Spring Boot | 2.1.3.RELEASE | Web应用框架 |
| **ORM** | MyBatis Plus | 3.5.5 | 持久层框架，逻辑删除、分页、代码生成 |
| **数据库连接池** | Druid | 1.2.20 | 阿里巴巴数据库连接池 |
| **关系型数据库** | MySQL | 8.0.33 | 业务数据存储，含全文索引(n-gram) |
| **缓存中间件** | Redis | 5.x (Lettuce) | 会话缓存(Session)、验证码、热点数据 |
| **安全框架** | Spring Security + JWT | auth0-java-jwt 3.10.3 | 认证授权，JWT 30分钟过期，BCrypt密码加密 |
| **AI集成框架** | LangChain4j | 0.29.1 | LLM调用编排、Prompt模板管理 |
| | langchain4j-open-ai | 0.29.1 | 兼容OpenAI接口的LLM调用 |
| | langchain4j-milvus | 1.0.0-beta3 | Milvus向量数据库集成 |
| **AI平台** | 阿里百炼 DashScope | qwen3.5-122b-a10b | 大语言模型API服务 |
| **向量数据库** | Milvus | 2.x | 向量存储与语义相似度检索 |
| **PDF解析** | Apache PDFBox | 2.0.29 | PDF文本提取（含pdfbox-tools图片处理） |
| **OCR识别** | Tesseract4j (Tess4J) | 5.8.0 | 图片文字识别，含chi_sim中文语言包 |
| | Lept4j | 1.12.0 | Tesseract依赖的Leptonica图像处理库 |
| **Office解析** | Apache POI | 4.1.2 | poi/poi-ooxml/poi-scratchpad，Word/Excel文档解析 |
| **图像处理** | JAI ImageIO | 1.4.0 | JPEG2000等高级图像格式支持 |
| **实时通信** | Spring WebSocket | 2.2.2 | 全双工长连接，替代Ajax轮询 |
| **对象存储** | MinIO | 7.0.1 | 分布式对象存储（可选） |
| | 阿里云OSS SDK | 3.10.2 | 阿里云对象存储（可选） |
| **对象映射** | MapStruct | 1.5.5.Final | Entity/VO/Form编译期转换 |
| **Excel处理** | EasyExcel | 3.3.4 | 批量导入导出 |
| **API文档** | Swagger2 + Knife4j | 2.9.2 / 2.0.4 | 在线API调试文档 |
| **工具库** | Hutool | 5.8.23 | 通用工具集 |
| | Fastjson | 1.2.83 | JSON序列化 |
| | Commons IO / Commons Collections4 | 2.6 / 4.4 | Apache通用工具 |
| | Lombok | 1.18.30 | 减少样板代码 |
| **支付集成** | 支付宝SDK | 4.39.218.ALL | Token充值支付 |
| **IP地址** | ip2region | 2.6.5 | IP地址解析为地区 |
| **构建工具** | Maven | 3.8+ | 项目构建与依赖管理 |
| **开发环境** | IntelliJ IDEA 2023.2 | — | 后端IDE |
| | JDK | 11 | Java运行环境 |

### 2.2 前端技术栈

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **核心框架** | Vue.js | 2.6.10 | 渐进式前端框架 |
| **UI组件库** | Element UI | 2.13.2 | 桌面端UI组件 |
| **路由管理** | Vue Router | 3.0.6 | 单页应用路由 |
| **状态管理** | Vuex | 3.1.0 | 全局状态管理（6个模块） |
| **HTTP客户端** | Axios | 0.18.1 | RESTful API请求 |
| **实时通信** | WebSocket（原生） | — | 聊天消息与通知推送 |
| **图表库** | ECharts | 4.9.0 | 数据可视化 |
| **富文本编辑器** | Vue Quill Editor | 3.0.6 | 公告/通知编辑 |
| **PDF导出** | jsPDF + html2canvas | 2.5.1 / 1.4.1 | 页面导出为PDF |
| **加密** | CryptoJS | 4.2.0 | 前端数据加密 |
| **JWT解析** | jwt-decode | 4.0.0 | Token载荷解析 |
| **Cookie操作** | js-cookie | 2.2.0 | Cookie读写 |
| **Mock数据** | MockJS | 1.0.1-beta3 | 开发阶段Mock数据（已注释） |
| **CSS预处理器** | Sass | 1.85.1 | SCSS样式编写 |
| **构建工具** | Vue CLI | 4.4.4 | 项目脚手架与构建 |
| **代码规范** | ESLint | 6.7.2 | 代码质量检查 |
| **开发环境** | VS Code 1.85 | — | 前端IDE |

---

## 3. 系统架构

### 3.1 五层B/S架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         展示层 (Presentation Layer)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │
│  │ 考试中心 │ │ RAG问答   │ │ AI阅卷    │ │ 学习平台 │ │ 管理后台          │ │
│  │ Vue 2.6  │ │ Element  │ │ Vuex     │ │ ECharts  │ │ Vue Router       │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────────┬─────────┘ │
│       │            │            │            │                │            │
│   ────┼────────────┼────────────┼────────────┼────────────────┼───────     │
│       │   HTTP/REST (Axios)           WebSocket 全双工通信     │            │
└───────┼────────────┼────────────┼────────────┼────────────────┼────────────┘
        │            │            │            │                │
┌───────┼────────────┼────────────┼────────────┼────────────────┼────────────┐
│       ▼            ▼            ▼            ▼                ▼            │
│                      Nginx 反向代理 (/api → :8080)                           │
└───────┬────────────┬────────────┬────────────┬────────────────┬────────────┘
        │            │            │            │                │
┌───────┼────────────┼────────────┼────────────┼────────────────┼────────────┐
│       ▼            ▼            ▼            ▼                ▼            │
│                      Spring Security 过滤器链 (JWT认证)                     │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   业务逻辑层 (Business Logic Layer)                    │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │   │
│  │  │ 考试服务 │ │ RAG服务  │ │ AI阅卷   │ │ 讨论服务 │ │ 用户服务 │  │   │
│  │  │ ExamSvc  │ │ RagSvc   │ │ AiGrading │ │ Discuss  │ │ UserSvc  │  │   │
│  │  └─────┬────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │   │
│  └────────┼───────────┼────────────┼────────────┼────────────┼────────┘   │
├───────────┼───────────┼────────────┼────────────┼────────────┼────────────┤
│  ┌────────▼───────────▼────────────▼────────────▼────────────▼────────┐   │
│  │                   数据访问层 (Data Access Layer)                     │   │
│  │                    MyBatis Plus Mapper (49个Mapper)                │   │
│  │              Model: Entity(54个) / VO / Form / DTO                 │   │
│  │              Converter: MapStruct 编译期对象转换 (16个)               │   │
│  └────────┬───────────┬────────────┬────────────┬────────────┬────────┘   │
├───────────┼───────────┼────────────┼────────────┼────────────┼────────────┤
│  ┌────────▼────┐ ┌────▼──────┐ ┌───▼──────┐ ┌──▼──────────────┐          │
│  │   MySQL 5.7 │ │  Redis 5  │ │ Milvus 2 │ │ 文件存储 (Local │          │
│  │  db_exam    │ │  缓存/会话  │ │ 向量检索   │ │  /Minio/OSS)    │          │
│  └─────────────┘ └───────────┘ └──────────┘ └─────────────────┘          │
│                       数据存储层 (Data Storage Layer)                      │
└───────────────────────────────────────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼─────────────────────────────────────────┐
│  ┌────────────────────────────────▼─────────────────────────────────────┐   │
│  │                      AI能力层 (AI Capability Layer)                   │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ │   │
│  │  │ LangChain4j  │ │ DashScope API│ │ Apache PDFBox│ │ Tesseract OCR│ │   │
│  │  │ LLM编排/Prompt│ │ qwen3.5模型   │ │ 文档文本提取   │ │ 图像文字识别    │ │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────────────┘
```

### 3.2 数据流总览

```
学生端浏览器                    后端服务器                         外部服务
─────────────                  ────────────                      ───────────
[上传资料] ──POST──→ [FileController] ──→ [文件存储本地/MinIO/OSS]
                          │
                          ├──→ [t_rag_parse_task] 插入异步任务
                          │         │
                          ▼         ▼ (定时轮询)
                   [RagParseTaskService] ──→ [PDFBox/POI/Tesseract] 文本提取
                          │
                          ├──→ 滑动窗口分块(512/128) → [t_rag_knowledge] 写入
                          │
                          ▼
                   [Milvus向量化存储]

[提问问题] ──POST──→ [RagController] ──→ [RagKnowledgeService]
                          │                    │
                          │              算法: TF-IDF + 余弦相似度
                          │              流程: 分词 → 关键词提取 → 双路检索
                          │                    │
                          │              召回 Top-5 知识块
                          │                    │
                          │               ─────▼──────
                          │               Prompt拼接  
                          │                模板: "基于 
                          │                以下资料回答
                          │                ..." + 问题 
                          │               ─────┬──────
                          │                    │
                          └────  返回答案 ───── ▼──→ [DashScope API]
                                   + 引用来源
```

---

## 4. 后端源码结构

后端项目位于 `online-exam-system-backend-master/`，基础包名 `cn.org.alan.exam`。

### 4.1 包结构总览与职责说明

```
cn.org.alan.exam
│
├── ExamApplication.java                    # Spring Boot 启动入口
│
├── annotation/                             # 【自定义注解】
│   └── RequireToken.java                   #   @RequireToken - Token校验标记注解
│                                           #   配合AOP实现声明式Token校验
│
├── aspect/                                 # 【切面层】
│   └── TokenCheckAspect.java               #   Token校验AOP切面
│                                           #   拦截@RequireToken注解的方法，
│                                           #   从请求头提取Authorization验证JWT
│
├── common/                                 # 【公共模块】
│   ├── aop/LogAsPect.java                  #   操作日志AOP切面
│   │                                       #   记录Controller方法调用日志
│   ├── config/DatabaseMigrationRunner.java #   数据库迁移启动器
│   │                                       #   实现ApplicationRunner，启动时自动
│   │                                       #   执行SQL迁移脚本
│   ├── exception/ServiceRuntimeException   #   自定义业务异常
│   ├── group/                              #   参数校验分组
│   │   ├── AnswerGroup.java                #   答题相关校验组
│   │   ├── CertificateGroup.java           #   证书相关校验组
│   │   ├── QuestionGroup.java              #   题目相关校验组
│   │   └── UserGroup.java                  #   用户相关校验组
│   ├── handler/                            #   异常处理
│   │   ├── FiledFullHandler.java           #   字段填充处理器
│   │   └── GlobalExceptionHandler.java     #   全局异常处理器
│   └── result/Result.java                  #   统一API响应封装 {code, msg, data}
│
├── config/                                 # 【配置层 - 11个配置类】
│   ├── AIConfig.java                       #   AI配置: DashScope API Key/URL/模型
│   ├── AlipayPayConfig.java                #   支付宝支付配置
│   ├── CorsConfig.java                     #   跨域CORS配置
│   ├── MultipartConfig.java                #   文件上传配置
│   ├── MybatisPlusConfig.java              #   MyBatis Plus分页插件/乐观锁
│   ├── OnlineExamConfig.java               #   系统自定义配置
│   │                                       #   (存储方式/验证码开关/自动阅卷开关)
│   ├── RedisConfig.java                    #   Redis序列化/连接池配置
│   ├── RestTemplateConfig.java             #   RestTemplate Bean配置
│   ├── SecurityConfig.java                 #   Spring Security核心配置
│   │                                       #   JWT过滤器注册/URL权限规则/BCrypt
│   ├── SwaggerConfig.java                  #   Knife4j API文档配置
│   ├── WebMvcConfig.java                   #   Spring MVC拦截器/资源映射配置
│   └── WebSocketConfig.java               #   WebSocket端点注册配置
│
├── controller/                             # 【控制器层 - 42个Controller】
│   │                                       # 职责: 接收HTTP请求，参数校验，
│   │                                       # 调用Service，返回统一Result
│   │                                       #
│   ├── AuthController.java                 #   登录/注册/验证码 (/auth)
│   ├── UserController.java                 #   用户管理 (/user)
│   ├── ExamController.java                 #   考试管理 (/exam)
│   ├── QuestionController.java             #   题目管理 (/question)
│   ├── RecordController.java               #   考试/练习记录 (/records)
│   ├── ScoreController.java                #   成绩管理 (/score)
│   ├── AnswerController.java               #   答卷管理 (/answers)
│   ├── AiGradingController.java            #   AI阅卷 (/ai-grading)
│   ├── AiChatController.java               #   AI对话 (/ai-chat)
│   ├── AiChatHistoryController.java        #   AI对话历史 (/ai-chat-history)
│   ├── AiConfigController.java             #   AI配置管理 (/ai-config)
│   ├── RagController.java                  #   RAG知识库 (/rag) - 核心
│   │                                       #   - 资料解析: POST /parse/{materialId}
│   │                                       #   - 任务进度: GET /task/{taskId}
│   │                                       #   - 知识库统计: GET /stats
│   │                                       #   - 检索测试: POST /test
│   │                                       #   - 清空知识库: DELETE /clear
│   ├── GradingRagController.java           #   阅卷RAG (/grading-rag)
│   ├── RealAiGradingController.java        #   真实AI阅卷 (/real-ai-grading)
│   ├── ChatMessageController.java          #   聊天消息 (/chat-message)
│   ├── DiscussionController.java           #   讨论交流 (/discussion)
│   ├── NoticeController.java               #   公告通知 (/notice)
│   ├── NotificationController.java         #   消息通知 (/notification)
│   ├── RepoController.java                 #   题库管理 (/repo)
│   ├── CategoryController.java             #   题目分类 (/category)
│   ├── CertificateController.java          #   证书管理 (/certificate)
│   ├── GradeController.java                #   班级管理 (/grades)
│   ├── StatController.java                 #   成绩统计 (/stat)
│   ├── LogController.java                  #   日志管理 (/log)
│   ├── HomeController.java                 #   首页数据 (/home)
│   ├── PersonalizedHomeController.java     #   个性化首页
│   ├── FileController.java                 #   文件上传 (/file)
│   ├── StudyMaterialController.java        #   学习资料 (/study-material)
│   ├── FriendRelationController.java       #   好友关系
│   ├── LikeController.java                 #   点赞
│   ├── UserBookController.java             #   错题本
│   ├── ExerciseController.java             #   刷题练习
│   ├── AvatarController.java               #   头像
│   ├── AdminAuditController.java           #   用户审核
│   ├── UserProfileController.java          #   用户画像
│   ├── UserQuestionnaireController.java    #   用户问卷
│   ├── AIQuestionController.java           #   AI题目推荐
│   ├── AIRecommendController.java          #   AI推荐
│   └── ...                                 #   其他控制器
│
├── service/                                # 【服务层 - 56个Service接口 + 52个实现】
│   │                                       # 职责: 业务逻辑封装，事务管理，
│   │                                       # 调用Mapper和AI能力层
│   │                                       #
│   ├── impl/                               #   服务实现类
│   │   ├── ExamServiceImpl.java            #   考试服务: 开始考试、交卷、
│   │   │                                   #     切屏检测、添加作弊记录、自动评分
│   │   ├── RagKnowledgeServiceImpl.java    #   ★ RAG知识库核心服务:
│   │   │                                   #     - parseAndStoreMaterial: 解析资料入库
│   │   │                                   #     - extractPdf: PDF文本提取(含OCR回退)
│   │   │                                   #     - preprocessImage: 图像预处理四步
│   │   │                                   #     - splitText: 滑动窗口分块
│   │   │                                   #     - tokenize: 中英文混合分词
│   │   │                                   #     - searchKnowledge: TF-IDF检索
│   │   ├── RealAiGradingServiceImpl.java   #   ★ AI阅卷核心服务:
│   │   │                                   #     - gradeWithRealAI: 单题评分
│   │   │                                   #     - batchGradeWithRealAI: 异步批量评分
│   │   │                                   #     - buildGradingPrompt: 四维度Prompt构建
│   │   │                                   #     - parseAiGradingResponse: 结果解析
│   │   │                                   #     - callRealAiGradingAPI: LLM API调用
│   │   ├── AutoScoringServiceImpl.java     #   自动评分服务(考试提交后异步触发)
│   │   ├── AiChatServiceImpl.java          #   AI对话服务
│   │   ├── EnhancedAiChatServiceImpl.java  #   增强AI对话(RAG混合检索)
│   │   ├── StudyMaterialServiceImpl.java   #   学习资料上传/下载/预览/展馆
│   │   ├── PersonalizedHomeServiceImpl.java#   个性化首页(学习分析/弱项统计)
│   │   ├── UserServiceImpl.java            #   用户管理
│   │   └── ...                             #   其他服务实现
│   └── I*.java                             #   Service接口定义
│
├── mapper/                                 # 【数据访问层 - 49个Mapper接口】
│   │                                       # 职责: MyBatis Plus映射，SQL操作
│   │                                       #
│   ├── ExamMapper.java                     #   考试表CRUD
│   ├── QuestionMapper.java                 #   题目表CRUD
│   ├── ExamQuAnswerMapper.java             #   答案表CRUD
│   ├── RagKnowledgeMapper.java             #   RAG知识库表CRUD
│   ├── RagParseTaskMapper.java             #   RAG解析任务表CRUD
│   ├── StudyMaterialMapper.java            #   学习资料表CRUD
│   └── ...                                 #   其他Mapper
│
├── model/                                  # 【数据模型层】
│   ├── entity/                             #   数据库实体 (54个)
│   │   ├── User.java                       #   用户实体
│   │   ├── Exam.java                       #   考试实体
│   │   ├── Question.java                   #   题目实体
│   │   ├── ExamQuAnswer.java               #   答案实体（含ai_score/ai_reason）
│   │   ├── RagKnowledge.java               #   RAG知识块实体
│   │   ├── RagParseTask.java               #   解析任务实体
│   │   ├── StudyMaterial.java              #   学习资料实体
│   │   └── ...
│   ├── vo/                                 #   视图对象（按模块分包，共60+个VO）
│   │   ├── record/                         #   考试/练习记录VO
│   │   ├── score/                          #   成绩VO（含ExportScoreVO导出）
│   │   ├── exam/                           #   考试VO（ExamDetailVO等8个）
│   │   ├── question/                       #   题目VO
│   │   ├── ai/                             #   AI相关VO
│   │   ├── home/                           #   首页VO（含个性化推荐）
│   │   └── ...
│   ├── form/                               #   表单/请求对象（按模块分包）
│   │   ├── exam/ExamAddForm.java           #   考试创建表单
│   │   ├── ai/AiGradingForm.java           #   AI阅卷表单
│   │   └── ...
│   ├── dto/Message.java                    #   WebSocket消息DTO
│   └── enums/                              #   枚举类
│       ├── ExamState.java                  #   考试状态枚举
│       ├── MessageType.java                #   消息类型枚举
│       └── QuestionType.java               #   题目类型枚举
│
├── converter/                              # 【对象转换器 - 16个MapStruct】
│   │                                       # 编译期生成Entity↔VO↔Form转换代码
│   ├── ExamConverter.java
│   ├── QuestionConverter.java
│   ├── RecordConverter.java
│   └── ...
│
├── filter/                                 # 【过滤器层】
│   └── VerifyTokenFilter.java              #   JWT Token验证过滤器
│                                           #   拦截所有API请求，验证Token有效性
│
├── handler/                                # 【处理器层】
│   └── ChatWebSocketHandler.java           #   WebSocket聊天消息处理器
│                                           #   处理消息发送、接收、转发
│
├── task/                                   # 【定时任务层】
│   └── ExamTask.java                       #   考试定时任务
│                                           #   扫描到期考试自动交卷
│
└── utils/                                  # 【工具类层】
    ├── JwtUtil.java                        #   JWT生成/解析/验证工具
    ├── SecurityUtil.java                   #   Spring Security上下文工具
    ├── CryptoUtils.java                    #   加密解密工具
    ├── DateTimeUtil.java                   #   日期时间格式化工具
    ├── IPUtils.java                        #   IP地址解析工具（ip2region）
    ├── JsonUtil.java                       #   JSON序列化工具
    ├── ResponseUtil.java                   #   HTTP响应工具
    ├── SecretUtils.java                    #   密钥生成工具
    ├── ClassTokenGenerator.java            #   班级TOKEN生成器
    ├── util/excel/                         #   Excel导入导出工具包
    │   ├── ExcelExport.java                #   Excel导出注解
    │   ├── ExcelImport.java                #   Excel导入注解
    │   └── ExcelUtils.java                 #   Excel处理工具
    └── util/file/                          #   文件存储工具包
        ├── FileService.java                #   文件服务接口
        ├── impl/LocalFileUtil.java         #   本地文件存储实现
        ├── impl/MinioUtil.java             #   MinIO文件存储实现
        └── impl/AliOSSUtil.java            #   阿里云OSS文件存储实现
```

### 4.2 资源文件结构

```
src/main/resources/
├── application.yml                         #   主配置（激活dev/profile）
├── application-dev.yml                     #   开发环境配置
├── application-prod.yml                    #   生产环境配置
├── mapper/                                 #   MyBatis XML映射文件 (25个)
│   ├── ExamMapper.xml
│   ├── QuestionMapper.xml
│   └── ...
├── db/                                     #   数据库迁移脚本
│   ├── migration/                          #   Flyway风格SQL迁移
│   │   ├── V2026_04_30_000001__init.sql
│   │   └── V2026_05_04_000001__update.sql
│   └── ai_config.sql                      #   AI配置初始化数据
├── sql/                                    #   Token配置SQL
├── tessdata/                               #   Tesseract中文语言包
│   └── chi_sim.traineddata                 #   简体中文训练数据
├── ipdata/ip2region.xdb                    #   IP地址库
└── static/default-avatar.jpg               #   默认头像
```

---

## 5. 前端源码结构

前端项目位于 `online-exam-system-frontend-master/`，基于 `vue-admin-template` 模板改造。

### 5.1 目录结构与职责

```
src/
├── main.js                                 #   【入口文件】
│                                           #   - 全局注册Vue、ElementUI、ECharts
│                                           #   - 挂载Router、Store、Icons
│                                           #   - 引入permission路由守卫
│                                           #   - 全局挂载WebSocket方法
│
├── App.vue                                 #   【根组件】
├── settings.js                             #   【全局设置】（网站标题等）
│
├── permission.js                           #   【路由权限守卫】
│   │                                       #   - 白名单: /login, /register, /404
│   │                                       #   - Token验证 → 获取用户信息
│   │                                       #   - 首次登录学生 → 跳转备考问卷
│   │                                       #   - 无Token → 重定向登录页
│   │                                       #   - NProgress加载进度条
│
├── router/index.js                         #   【路由配置】
│   │                                       #   - 基于@/layout主布局
│   │                                       #   - 按角色组织路由:
│   │                                       #     · 学生: 首页/问卷/考试/刷题/错题本/AI问答等
│   │                                       #     · 教师: 班级/考试/题库/阅卷/成绩统计
│   │                                       #     · 管理员: AI配置/Token管理/用户审核/阅卷RAG
│   │                                       #   - 根路径按角色自动重定向
│
├── store/                                  #   【Vuex状态管理】
│   ├── index.js                            #     根Store: 登录状态/心跳机制(每5分钟)
│   ├── getters.js                          #     全局Getters
│   └── modules/                            #     6个模块:
│       ├── user.js                         #       用户信息/Token/登录/登出/WebSocket
│       ├── app.js                          #       sidebar状态/设备类型
│       ├── settings.js                     #       全局设置
│       ├── menu.js                         #       菜单/标签页管理
│       ├── theme.js                        #       主题配置
│       └── aiModel.js                      #       AI模型选择
│
├── api/                                    #   【API接口层 - 41个模块】
│   │                                       #   每个模块封装一类API:
│   ├── exam.js                             #     考试CRUD/开始考试/交卷/切屏标记
│   ├── question.js                         #     题目CRUD/批量导入
│   ├── answer.js                           #     答卷查询/批阅
│   ├── rag.js                              #     RAG解析/进度查询/检索测试/知识库管理
│   ├── ai-chat.js                          #     AI对话
│   ├── ai-grading.js                       #     AI阅卷(教师端)
│   ├── ai-grading-result.js                #     AI阅卷结果(学生端)
│   ├── ai-config.js                        #     AI模型参数配置(管理员)
│   ├── grading-rag.js                      #     阅卷RAG(教师上传评分参考)
│   ├── record.js                           #     考试/练习记录
│   ├── score.js                            #     成绩管理/导出
│   ├── repo.js                             #     题库管理
│   ├── category.js                         #     题目分类
│   ├── exercise.js                         #     刷题练习
│   ├── user.js                             #     用户管理/个人信息
│   ├── user-profile.js                     #     用户画像
│   ├── user-questionnaire.js               #     用户问卷
│   ├── userbook.js                         #     错题本
│   ├── certificate.js                      #     证书管理
│   ├── class_.js                           #     班级管理
│   ├── discuss.js                          #     讨论区
│   ├── chat.js                             #     聊天
│   ├── notice.js                           #     公告
│   ├── notification.js                     #     通知
│   ├── home.js                             #     首页数据
│   ├── personalized-home.js                #     个性化首页
│   ├── study-material.js                   #     学习资料
│   ├── like.js                             #     点赞
│   ├── log.js                              #     系统日志
│   ├── stat.js                             #     统计数据
│   ├── token.js                            #     Token充值/查询
│   ├── token-consume-config.js             #     Token消费配置
│   ├── alipay-config.js                    #     支付宝配置
│   ├── admin-grading-rag.js                #     管理员阅卷RAG
│   └── common.js                           #     通用(验证码等)
│
├── utils/                                  #   【工具函数层】
│   ├── request.js                          #     ★ Axios实例配置 (核心)
│   │                                       #     - baseURL: 环境变量VUE_APP_BASE_API
│   │                                       #     - 请求拦截: 自动携带Authorization Token
│   │                                       #     - 响应拦截: 自动更新Token/统一错误处理
│   │                                       #     - 超时60秒(适应AI生成)
│   │                                       #     - 401→跳登录, 403→无权限提示
│   ├── websocket.js                        #     ★ WebSocket客户端 (核心)
│   │                                       #     - 连接: ws://localhost:8080/api/websocket?userId=X
│   │                                       #     - 指数退避自动重连(最多10次)
│   │                                       #     - 消息事件总线分发
│   │                                       #     - 公告通知弹出
│   ├── auth.js                             #     Token存取(Cookie)
│   ├── CryptoJS.js                         #     加密工具
│   ├── Secret.js                           #     密钥配置
│   ├── jwtUtils.js                         #     JWT解析工具
│   ├── eventBus.js                         #     事件总线(Vue实例)
│   ├── get-page-title.js                   #     页面标题获取
│   ├── htmlToPdf.js                        #     HTML转PDF导出
│   ├── scroll-to.js                        #     滚动动画
│   ├── validate.js                         #     表单校验规则
│   └── messagePopup.js                     #     消息弹窗
│
├── layout/                                 #   【布局组件】
│   ├── index.vue                           #     主布局(sidebar + navbar + app-main)
│   └── components/                         #     布局子组件
│       ├── AppMain.vue                     #       主内容区(router-view)
│       ├── Navbar.vue                      #       顶部导航栏
│       └── Sidebar/                        #       侧边栏菜单
│
├── components/                             #   【可复用组件 - 15个】
│   ├── AiGrading/                          #     AI阅卷组件
│   │   ├── AiGradingDialog.vue             #       AI评分对话框
│   │   └── AiChatBubble.vue                #       AI答疑悬浮气泡
│   ├── ExamTimer/index.vue                 #     考试倒计时组件
│   ├── FileUpload/                         #     文件上传组件
│   ├── RichTextEditor/index.vue            #     富文本编辑器
│   ├── Breadcrumb/                         #     面包屑导航
│   ├── Hamburger/                          #     侧边栏折叠按钮
│   ├── SvgIcon/                            #     SVG图标组件
│   ├── ClassSelect/                        #     班级选择器
│   ├── RepoSelect/                         #     题库选择器
│   ├── CertificateSelect/                  #     证书选择器
│   ├── MessagePopup/                       #     消息弹窗
│   ├── discussion/                         #     讨论组件
│   └── user-questionnaire/                 #     问卷表单
│
├── views/                                  #   【页面视图 - 46个页面】
│   ├── login/                              #     登录/注册
│   ├── dashboard/                          #     首页仪表盘
│   │   └── rolePage/                       #       角色专属首页
│   │       ├── student.vue                 #         学生首页
│   │       ├── student-new.vue             #         学生新版首页
│   │       ├── ai-student-dashboard.vue    #         AI学习仪表盘
│   │       ├── ai-learning-platform.vue    #         AI学习平台
│   │       └── adminAndTeacher.vue         #         教师/管理员首页
│   ├── exam/                               #     考试模块
│   │   ├── student/index.vue               #       学生考试页(核心)
│   │   │                                   #       - 题目切换/答案暂存
│   │   │                                   #       - 切屏检测(visibilitychange)
│   │   │                                   #       - 倒计时/自动交卷
│   │   └── teacher/                        #       教师考试管理
│   ├── question/                           #     题目管理(CRUD)
│   ├── repo/                               #     题库管理
│   ├── exercise/                           #     刷题练习
│   ├── record/                             #     考试/练习记录详情
│   ├── score/                              #     成绩查看/导出
│   ├── answer/                             #     教师答卷批阅
│   ├── ai-grading/                         #     AI阅卷(触发/结果查看)
│   ├── ai-chat/                            #     AI对话助手
│   ├── ai-config/                          #     AI配置(管理员)
│   ├── rag/                                #     RAG解析报告
│   ├── admin-grading-rag/                  #     管理员阅卷RAG
│   ├── study-material/                     #     学习资料上传/展馆
│   ├── class/                              #     班级管理
│   ├── discuss/                            #     讨论交流(发帖/回帖/点赞)
│   ├── chat/                               #     好友聊天
│   ├── notice/                             #     公告
│   ├── notifications/                      #     消息通知
│   ├── certificate/                        #     证书管理
│   ├── userbook/                           #     错题本/重刷
│   ├── user/                               #     个人中心/问卷/修改密码
│   ├── token/                              #     Token管理(管理员/T充值)
│   ├── practice/                           #     单题练习
│   ├── log/                                #     系统日志
│   ├── admin/                              #     用户审核
│   └── 404.vue                             #     404 页面
│
├── styles/                                 #   【样式文件】
│   ├── index.scss                          #     全局样式
│   ├── element-ui.scss                     #     Element UI覆盖
│   ├── sidebar.scss                        #     侧边栏样式
│   ├── theme.scss                          #     主题变量
│   ├── glass.scss                          #     玻璃态效果(CSS3 backdrop-filter)
│   ├── transition.scss                     #     过渡动画
│   ├── mixin.scss                          #     SCSS混入
│   └── variables.scss                      #     SCSS变量
│
└── icons/                                  #   SVG图标资源
```

---

## 6. 各功能处理流程详解

### 6.1 RAG知识库构建流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          RAG知识库构建完整流程                                │
│                                                                             │
│  用户上传文件 (PDF/Word/图片)                                                │
│       │                                                                     │
│       ▼                                                                     │
│  ┌──────────────┐                                                           │
│  │ 前端校验      │  FileUpload组件: 校验文件类型、大小(<200MB)               │
│  └──────┬───────┘                                                           │
│         │ POST /study-material/upload                                       │
│         ▼                                                                   │
│  ┌──────────────┐                                                           │
│  │ 后端接收      │  StudyMaterialController.upload                          │
│  │ 持久化存储    │  → UUID重命名 → 写入本地/MinIO/OSS                       │
│  │              │  → t_study_material 插入记录                              │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼ (用户手动触发解析)                                                │
│  ┌──────────────┐                                                           │
│  │ 异步解析任务  │  POST /rag/parse/{materialId}                            │
│  │ 创建          │  → t_rag_parse_task 插入(状态=待处理)                    │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼ (守护线程执行，不阻塞主请求)                                       │
│  ┌──────────────────────────────────────────────────────────────┐           │
│  │ 文档类型判断 → 调用对应解析器                                  │           │
│  │                                                               │           │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌──────────┐     │           │
│  │  │ PDF (打印版)     │  │ PDF (扫描版)/图片 │  │ Word文档  │     │           │
│  │  │                 │  │                  │  │           │     │           │
│  │  │ Apache PDFBox   │  │ Tesseract OCR    │  │Apache POI │     │           │
│  │  │ 文本流提取      │  │                  │  │段落/标题  │     │           │
│  │  │ (文字层>200字)  │  │ 图像预处理四步:  │  │提取       │     │           │
│  │  │                 │  │ ①灰度转换        │  │           │     │           │
│  │  │ 不足→回退OCR    │  │ ②直方图拉伸     │  │           │     │           │
│  │  │                 │  │ ③Otsu二值化     │  │           │     │           │
│  │  │                 │  │ ④中值滤波降噪   │  │           │     │           │
│  │  │                 │  │ → Tesseract识别  │  │           │     │           │
│  │  └────────┬────────┘  └────────┬─────────┘  └─────┬─────┘     │           │
│  └───────────┼────────────────────┼──────────────────┼───────────┘           │
│              │                    │                  │                       │
│              └────────────────────┼──────────────────┘                       │
│                                   ▼                                          │
│  ┌──────────────────────────────────────────────────────────────┐           │
│  │ 滑动窗口文本分块                                              │           │
│  │                                                               │           │
│  │  窗口大小: 512字符  重叠区域: 128字符                        │           │
│  │                                                               │           │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                    │           │
│  │  │ 文本块0  │──│ 文本块1  │──│ 文本块2  │──...               │           │
│  │  │ 512字    │  │ 128重叠  │  │ 512字    │                    │           │
│  │  └──────────┘  └──────────┘  └──────────┘                    │           │
│  │                                                               │           │
│  │  每个块记录: content + chunk_index + material_id + user_id   │           │
│  └──────────────────────────┬───────────────────────────────────┘           │
│                             │                                               │
│                             ▼                                               │
│  ┌──────────────────────────────────────────────────────────────┐           │
│  │ 双路存储                                                      │           │
│  │                                                               │           │
│  │  ① MySQL (t_rag_knowledge) ← 全文索引(n-gram分词) + TF-IDF │           │
│  │  ② Milvus 向量数据库      ← LangChain4j向量化存储            │           │
│  └──────────────────────────┬───────────────────────────────────┘           │
│                             │                                               │
│                             ▼                                               │
│                     更新 t_rag_parse_task                                  │
│                     status=已完成, progress=100%                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 RAG智能问答流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          RAG智能问答完整流程                                  │
│                                                                             │
│  学生输入问题                                                               │
│       │                                                                     │
│       ▼                                                                     │
│  ┌──────────────┐                                                           │
│  │ 问题预处理    │  分词(Tokenization):                                      │
│  │              │  - 英文: 按空格/标点切分词                                 │
│  │              │  - 中文: 单字 + Bigram(2-gram) + Trigram(3-gram)          │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼                                                                   │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 双路检索策略                                               │               │
│  │                                                           │               │
│  │  ┌─────────────────────┐  ┌──────────────────────────┐   │               │
│  │  │ 路径一: 关键词检索   │  │ 路径二: 语义向量检索      │   │               │
│  │  │                     │  │                          │   │               │
│  │  │ MySQL全文索引       │  │ Milvus 向量数据库        │   │               │
│  │  │ n-gram(Bigram)分词  │  │ LangChain4j Embedding    │   │               │
│  │  │ TF-IDF 相关性计算   │  │ 余弦相似度 Top-K        │   │               │
│  │  │ 余弦相似度排序      │  │                          │   │               │
│  │  └─────────┬───────────┘  └────────────┬─────────────┘   │               │
│  │            │                           │                  │               │
│  │            └──────────┬────────────────┘                  │               │
│  │                       ▼                                   │               │
│  │               结果融合 & 重排序                            │               │
│  │               取 Top-K (默认K=5)                           │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ Prompt模板构建 (结构化拼接)                                │               │
│  │                                                           │               │
│  │  "你是一个专业的备考辅导助手。                              │               │
│  │   请根据以下参考资料回答用户的问题。                        │               │
│  │   如果你不确定或者参考资料中没有直接相关的信息，             │               │
│  │   请如实说明，不要编造。                                   │               │
│  │                                                           │               │
│  │   参考资料：                                               │               │
│  │   [知识块1] ...                                            │               │
│  │   [知识块2] ...                                            │               │
│  │   ...                                                     │               │
│  │   [知识块K]                                                │               │
│  │                                                           │               │
│  │   用户问题：[原始问题]                                     │               │
│  │   请确保回答准确、简洁并标明引用来源。"                    │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ DashScope API 调用                                        │               │
│  │                                                           │               │
│  │  POST {baseUrl}/chat/completions                          │               │
│  │  模型: qwen3.5-122b-a10b                                  │               │
│  │  参数: temperature=0.7, max_tokens=2000                    │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 响应返回前端                                               │               │
│  │  - 增强答案文本                                           │               │
│  │  - 引用知识块来源标注                                     │               │
│  │  - 保存到对话历史                                         │               │
│  └──────────────────────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.3 AI智能阅卷流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          AI智能阅卷完整流程                                   │
│                                                                             │
│  教师进入成绩管理 → 点击"AI阅卷"                                              │
│       │                                                                     │
│       ▼                                                                     │
│  ┌──────────────┐                                                           │
│  │ 查询待评分    │  SELECT * FROM t_exam_qu_answer                          │
│  │ 主观题答案    │  WHERE exam_id=X AND question_type=4                     │
│  │              │  AND (ai_score IS NULL OR ai_score=0)                     │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼                                                                   │
│  ┌──────────────┐                                                           │
│  │ 按题目分组    │  同一题的所有学生答案归为一组                              │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼                                                                   │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 四维度评分Prompt构建 (角色扮演 + 思维链)                    │               │
│  │                                                           │               │
│  │  "你是一位认真负责的阅卷老师，请根据以下标准评分：          │               │
│  │                                                           │               │
│  │   评分维度与权重：                                         │               │
│  │   ① 内容完整度 (40%): 是否覆盖所有关键要点                 │               │
│  │   ② 准确性 (30%):   知识点是否正确无误                     │               │
│  │   ③ 逻辑性 (15%):   论述是否条理清晰、逻辑自洽             │               │
│  │   ④ 深度 (15%):     是否有深入分析或独到见解               │               │
│  │                                                           │               │
│  │   题目：[题目内容]                                        │               │
│  │   标准答案：[参考答案]                                     │               │
│  │   [可选: RAG阅卷参考知识块]                                │               │
│  │   学生答案：[学生作答内容]                                 │               │
│  │                                                           │               │
│  │   请给出：                                                 │               │
│  │   评分：[0-100分]                                         │               │
│  │   是否合格：[合格/不合格]                                  │               │
│  │   评分依据：[简要说明]                                     │               │
│  │   详细分析：[各维度分析]                                   │               │
│  │   改进建议：[具体建议]                                     │               │
│  │   知识点：[涉及的知识点]                                   │               │
│  │   学习路径：[建议的学习方向]"                              │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 批量异步调用 (线程池)                                      │               │
│  │                                                           │               │
│  │  ExecutorService: max 3 threads                           │               │
│  │  每道题超时: 120秒                                        │               │
│  │  失败重试: 最多2次                                        │               │
│  │                                                           │               │
│  │  POST {baseUrl}/chat/completions                          │               │
│  │  temperature=0.3 (降低随机性保证评分稳定)                  │               │
│  │  max_tokens=3000                                          │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 响应解析 parseAiGradingResponse()                          │               │
│  │                                                           │               │
│  │  按行关键词解析:                                           │               │
│  │  "评分：" → score                                         │               │
│  │  "是否合格：" → qualified                                  │               │
│  │  "评分依据：" → reason                                    │               │
│  │  "详细分析：" → analysis                                   │               │
│  │  "改进建议：" → suggestions                                │               │
│  │  "知识点：" → knowledgePoints                              │               │
│  │  "学习路径：" → learningPath                               │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 分数转换 & 持久化                                          │               │
│  │                                                           │               │
│  │  实际得分 = AI百分制得分 × 题目分值 / 100                  │               │
│  │                                                           │               │
│  │  UPDATE t_exam_qu_answer SET                              │               │
│  │    ai_score = 转换后得分,                                  │               │
│  │    ai_reason = 评语+分析+建议                              │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│                          ▼                                                  │
│  ┌──────────────┐                                                           │
│  │ 教师复核      │  可选: 查看AI评分 → 确认/手动修改分数                     │
│  │              │  修改后: t_manual_score 记录人工修改                      │
│  │              │  标记: "AI独立评分" / "教师修改后评分"                     │
│  └──────┬───────┘                                                           │
│         │                                                                   │
│         ▼                                                                   │
│  ┌──────────────┐                                                           │
│  │ 总分汇总      │  客观题 + AI主观题得分                                    │
│  │              │  更新 t_user_exams_score                                   │
│  └──────────────┘                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.4 在线考试与防作弊流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      在线考试与防作弊完整流程                                  │
│                                                                             │
│  学生进入考试                                                               │
│       │                                                                     │
│       ▼                                                                     │
│  ┌──────────────────────────────────────┐                                   │
│  │ 前置条件检查                          │                                   │
│  │                                      │                                   │
│  │ ① 已登录 & 学生角色                   │                                   │
│  │ ② 考试在有效时间范围内                │                                   │
│  │ ③ 考试已发布至所在班级                │                                   │
│  │ ④ 本场切屏累计未超阈值                │                                   │
│  │ ⑤ 未重复开始同一考试                  │                                   │
│  └──────────┬───────────────────────────┘                                   │
│             │                                                               │
│             ▼                                                               │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 考试开始 ExamServiceImpl.startExam()                       │               │
│  │                                                           │               │
│  │  - 创建 UserExamsScore 记录 (state=0)                     │               │
│  │  - 前端加载全部试题                                         │               │
│  │  - 启动倒计时组件 (ExamTimer)                               │               │
│  └───────────────────────┬──────────────────────────────────┘               │
│                          │                                                  │
│         ┌────────────────┼────────────────┐                                 │
│         ▼                ▼                ▼                                 │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────┐                           │
│  │ 前端防护  │  │ 切屏检测      │  │ 后端兜底     │                           │
│  │          │  │              │  │              │                           │
│  │ 路由守卫  │  │ visibility-  │  │ 定时任务扫描  │                           │
│  │ 禁用地址栏│  │ change事件   │  │ 到期未交卷   │                           │
│  │ 跳转     │  │ 监听窗口失焦 │  │ → 自动提交   │                           │
│  │          │  │              │  │              │                           │
│  │ 禁用     │  │ 每次切屏→    │  │              │                           │
│  │ Ctrl+C/V │  │ POST上报后端 │  │              │                           │
│  │ 禁用右键 │  │              │  │              │                           │
│  └──────────┘  └──────┬───────┘  └──────────────┘                           │
│                        │                                                    │
│                        ▼                                                    │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 后端切屏处理 ExamServiceImpl.addCheat()                     │               │
│  │                                                           │               │
│  │  1. 查询当前考试 UserExamsScore                            │               │
│  │  2. 获取考试配置 maxCount (默认5次)                         │               │
│  │  3. 当前切屏计数 + 1                                        │               │
│  │  4. IF 当前计数 >= maxCount:                                │               │
│  │       → 强制交卷 handExam(examId)                           │               │
│  │       → 前端弹出警告 "考试因违规已终止"                     │               │
│  │       → 返回首页                                            │               │
│  │     ELSE:                                                  │               │
│  │       → 返回警告: "请勿切屏，最大X次,已切Y次"               │               │
│  └──────────────────────────────────────────────────────────┘               │
│                                                                             │
│                                    学生正常/违规交卷                          │
│                                        │                                     │
│                                        ▼                                     │
│  ┌──────────────────────────────────────────────────────────┐               │
│  │ 自动交卷逻辑 handExam()                                     │               │
│  │                                                           │               │
│  │  1. 未作答简答题 → 插入空白作答记录                         │               │
│  │  2. 客观题自动判分:                                        │               │
│  │     - 单选: 对比选项ID                                     │               │
│  │     - 多选: 选项ID集合完全匹配                              │               │
│  │     - 判断: is_right = 答案与标准答案一致                   │               │
│  │  3. 错题 → 加入错题本 (t_user_book)                        │               │
│  │  4. 更新 UserExamsScore: state=1, 计算得分, 记录用时        │               │
│  │  5. IF 启用AI自动阅卷:                                      │               │
│  │       异步调用 autoScoringService.autoScoringExam()          │               │
│  │        → 主观题进入AI评分队列                                │               │
│  │  6. IF 考试关联证书 & 达到及格线:                            │               │
│  │       自动颁发证书                                          │               │
│  └──────────────────────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.5 AI实时对话流程

```
学生打开AI聊天页面
       │
       ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ 输入消息      │────→│ WebSocket    │────→│ ChatWebSocket│
│ (前端)       │     │ 连接         │     │ Handler      │
│              │     │ ws://host/api│     │ (后端)       │
│              │     │ /websocket   │     │              │
└──────────────┘     └──────────────┘     └──────┬───────┘
                                                 │
          ┌──────────────────────────────────────┼──────────┐
          ▼                                      ▼          │
   ┌──────────────┐                    ┌──────────────┐     │
   │ 普通聊天消息  │                    │ AI增强对话    │     │
   │ 直接转发      │                    │              │     │
   │ t_chat_message│                    │ → 后端转发至 │     │
   │ 持久化       │                    │ DashScope API│     │
   └──────────────┘                    │ → 生成回复    │     │
                                       │ → 持久化      │     │
                                       └──────┬───────┘     │
                                              │             │
          ┌───────────────────────────────────┘             │
          ▼                                                 │
   ┌──────────────┐                                         │
   │ WebSocket    │                                         │
   │ 推送回复给   │                                         │
   │ 接收方前端   │                                         │
   └──────────────┘                                         │
```

### 6.6 个性化学习路径生成流程

```
学生注册
    │
    ▼
┌──────────────┐
│ 备考目标问卷  │  收集信息:
│ (Onboarding  │  - 学习目标 (考研/考公/考证)
│ Questionnaire)│  - 目标院校/岗位
│              │  - 薄弱科目
│              │  - 每日可用学习时间
│              │  - 当前学习阶段
└──────┬───────┘
       │ 提交 → t_user_profile 持久化
       ▼
┌──────────────┐
│ DashScope API │  Prompt = 用户画像 + "请生成个性化学习计划"
│ 生成学习计划  │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌──────────────────┐
│ 个性化首页    │←────│ 动态调整:        │
│              │     │ - 用户做题数据    │
│ - 推荐课程   │     │ - 错题本分析      │
│ - 学习计划   │     │ - RAG问答记录     │
│ - 弱项分析   │     │ - 考试/练习成绩   │
│ - 学习时长   │     └──────────────────┘
└──────────────┘
```

---

## 7. 核心算法与代码

### 7.1 图像预处理算法（OCR前处理）

图像预处理在 `RagKnowledgeServiceImpl.preprocessImage()` 中实现，包含四个步骤：

```
算法流程:
  输入: 原始扫描图像 (BufferedImage)
  输出: 预处理后的二值图像

  Step 1: 灰度转换 (ColorConvertOp 硬件加速)
          grayImage = new BufferedImage(w, h, TYPE_BYTE_GRAY)
          ColorConvertOp(RGB→GRAY).filter(原始图, 灰度图)
          耗时: 3.8ms (优化前 45.2ms, 加速比 11.9×)

  Step 2: 直方图拉伸对比度增强
          遍历像素, 计算灰度直方图 [0,255]
          找到累积像素5%处的低阈值 lowPercent
          找到累积像素95%处的高阈值 highPercent
          线性映射: 新值 = (原值 - lowPercent) × 255 / (highPercent - lowPercent)
          耗时: 2.1ms (新增优化步骤)

  Step 3: Otsu大津法自适应二值化
          目标: 最大化类间方差 σ²(t) = ω₀(t)·ω₁(t)·[μ₀(t) - μ₁(t)]²
          遍历0-255所有阈值t, 计算σ²(t)
          取最优阈值 t* = argmax σ²(t)
          像素 > t* → 白色(255), 否则 → 黑色(0)
          耗时: 4.2ms (优化前 128.5ms, 加速比 30.6×)

  Step 4: 中值滤波降噪 (3×3 窗口)
          对每个像素，取3×3邻域中值替换
          有效去除椒盐噪声，保留文字边缘
          耗时: 8.5ms (优化前 35.7ms, 加速比 4.2×)

  总耗时: 18.6ms (优化前 209.4ms, 加速比 11.3×)
  OCR准确率: 72.6% → 86.4% (+13.8%)
```

**关键参数与性能数据:**

| 测试样本 | 优化前准确率 | 优化后准确率 | 提升 |
|---------|------------|------------|------|
| 清晰打印PDF | 94.2% | 95.1% | +0.9% |
| 轻度扫描件 | 82.5% | 91.3% | +8.8% |
| 中度扫描件 | 68.4% | 86.7% | +18.3% |
| 重度扫描件 | 45.2% | 72.4% | +27.2% |

### 7.2 滑动窗口文本分块算法

```
算法: SlidingWindowChunking
输入: 文本T, 窗口大小 chunkSize=512, 重叠 overlap=128
输出: 文本块列表 chunks[]

伪代码:
  chunks = []
  paragraphs = 按段落分割(T)
  current_chunk = ""
  
  FOR each paragraph IN paragraphs:
    IF len(current_chunk + paragraph) <= chunkSize:
      current_chunk += paragraph
    ELSE:
      chunks.add(current_chunk)                           // 保存当前块
      // 取当前块末尾overlap个字符 + 新段落作为下一块起点
      overlap_text = current_chunk[-overlap:]             // 取末尾128字符
      current_chunk = overlap_text + paragraph            // 语义连续
  
  IF current_chunk 非空:
    chunks.add(current_chunk)                             // 保存最后一个块
  
  RETURN chunks

实现说明:
  - 每个块记录: content(文本), chunk_index(序号), material_id(来源资料), user_id(所属用户)
  - 512字符可承载一个完整知识点，128重叠保证边界语义连续
  - 按段落切分优先，避免在句中断开
```

### 7.3 中英文混合分词算法

```
算法: Tokenize
输入: 文本 text
输出: 词语列表 tokens[]

处理逻辑:
  1. 判断字符类型:
     - 英文字母/数字 → 按空格和标点符号切分
     - 中文字符 → 按以下粒度提取:
       a) 单字 (unigram):       每个中文字作为一个token
       b) 双字 (bigram):        相邻2个中文字作为一个token
       c) 三字 (trigram):       相邻3个中文字作为一个token
  
  2. 示例: "机器学习是人工智能的核心"
     → ["机","器","学","习","是","人","工","智","能","的","核","心",
        "机器","器学","学习","习是","是人","人工","工智","智能","能的","的核","核心",
        "机器学","器学习","学习是","习是人","是人工","人工智","工智能","智能的","能的核","的核心"]

  目的:
  - 单字保证召回率（不会漏掉单字关键词）
  - Bigram/Trigram提高精确率（"人工智能"作为词组比"人"+"工"+"智"+"能"更有意义）
  - 英文词保留完整语义
```

### 7.4 TF-IDF + 余弦相似度检索算法

```
算法: TF-IDF 检索
输入: 查询Q, 知识库集合D (用户的知识块)
输出: 按相关度排序的 Top-K 知识块

Step 1: 分词
  query_tokens = tokenize(Q)
  对每个文档d in D:  doc_tokens[d] = tokenize(d.content)

Step 2: 计算TF-IDF向量
  词汇表V = 所有文档中出现的所有token
  
  对每个token t in V:
    对每个文档d:
      TF(t,d) = count(t in d) / len(d)           # 词频
    IDF(t) = log((N + 1) / (df(t) + 1)) + 1      # 逆文档频率(平滑)
    
  query_vector = [TF(t, Q) × IDF(t) for t in V]
  doc_vectors[d] = [TF(t, d) × IDF(t) for t in V]

Step 3: 余弦相似度计算
  对每个文档d:
    sim(Q, d) = (query_vector · doc_vector[d]) / (|query_vector| × |doc_vector[d]|)
    
Step 4: 排序返回
  按sim(Q, d)降序排列
  RETURN Top-K 个 (知识块, 相似度)

MySQL全文索引辅助:
  - 使用 MATCH(chunk_content) AGAINST('关键词' IN BOOLEAN MODE)
  - n-gram分词器: Bigram粒度 (ngram_token_size=2)
  - 配合Java层TF-IDF做二次精排
```

### 7.5 AI阅卷评分算法

```
算法: 四维度主观题评分
输入: 题目Q, 标准答案S, 学生答案A, 可选RAG知识参考R
输出: 评分结果 {score, qualified, reason, analysis, suggestions, knowledgePoints}

Step 1: 构建评分Prompt (角色扮演 + 思维链)
  role = "你是一位认真负责的阅卷老师"
  dimensions = [
    {name: "内容完整度", weight: 0.40, desc: "是否覆盖所有关键要点"},
    {name: "准确性",     weight: 0.30, desc: "知识点是否正确无误"},
    {name: "逻辑性",     weight: 0.15, desc: "论述是否条理清晰"},
    {name: "深度",       weight: 0.15, desc: "是否有深入分析或独到见解"}
  ]
  
  prompt = f"""
  {role}
  评分标准: {dimensions}
  题目: {Q.content}
  标准答案: {S}
  {RAG参考: R}
  学生答案: {A}
  
  请按以下格式输出:
  评分：[0-100的整数]
  是否合格：[合格/不合格]
  评分依据：[简要]
  详细分析：[各维度分析]
  改进建议：[具体建议]
  知识点：[涉及知识点]
  学习路径：[建议]
  """

Step 2: LLM调用
  response = 调用 DashScope API (
    model: "qwen3.5-122b-a10b",
    messages: [{role: "user", content: prompt}],
    temperature: 0.3,     // 低温度保证评分稳定性
    max_tokens: 3000
  )

Step 3: 解析响应
  按行关键词正则提取:
    score = extractNumber(行含"评分")
    qualified = extractText(行含"是否合格")
    reason = extractText(行含"评分依据")
    analysis = extractText(行含"详细分析")
    suggestions = extractText(行含"改进建议")
    knowledgePoints = extractText(行含"知识点")
    learningPath = extractText(行含"学习路径")

Step 4: 分数转换
  actualScore = round(score × Q.score / 100)  // AI百分制 → 题目实际分值

Step 5: 持久化
  UPDATE t_exam_qu_answer SET
    ai_score = actualScore,
    ai_reason = reason + "\n" + analysis + "\n" + suggestions
  WHERE id = answerId

验证数据:
  AI评分 vs 人工评分 Spearman相关系数 = 0.87
```

### 7.6 防作弊切屏检测算法

```
算法: 轻量级切屏防作弊
触发: 浏览器 visibilitychange 事件

前端 (Vue):
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) {  // 页面失去焦点
      发送 POST /exam/cheat { examId }  到后端
      显示警告提示
    }
  });

后端 (ExamServiceImpl.addCheat):
  输入: examId
  处理:
    record = SELECT userExamsScore WHERE exam_id=examId AND user_id=currentUserId
    maxCount = exam.maxCount (default: 5)
    
    IF record.cheatCount >= maxCount:
      handExam(examId)   // 自动强制交卷
      RETURN "考试因违规已终止"
    ELSE:
      record.cheatCount++
      RETURN f"请勿切屏，最大切屏次数：{maxCount}, 已切屏次数：{record.cheatCount}"

后端兜底 (ExamTask):
  定时任务扫描:
    SELECT * FROM t_exam WHERE end_time < NOW()
    FOR each exam:
      查询所有 state=0 的 UserExamsScore 记录
      逐一执行 handExam() 自动提交
```

---

## 8. 数据库设计

### 8.1 数据库ER图核心关系

```
  t_user (用户)                    t_exam (考试)
  ┌─────────────┐                 ┌─────────────┐
  │ id (PK)     │                 │ id (PK)     │
  │ username    │                 │ title       │
  │ password    │                 │ exam_duration│
  │ role        │──创建──────────→│ gross_score │
  │ grade_id (FK)│    (教师)      │ passed_score│
  └──────┬──────┘                 │ max_count   │
         │                        │ user_id(FK) │
         │                        └──────┬──────┘
         │ 参加考试                       │ 包含
         ▼                               ▼
  ┌──────────────┐              ┌──────────────┐       ┌──────────────┐
  │user_exams_   │              │ exam_question│       │  question    │
  │score         │              │ (关联表)      │──────→│  (题目)      │
  │              │              └──────────────┘       │ id (PK)     │
  │ user_id (FK) │                                     │ type (1-4)  │
  │ exam_id (FK) │                                     │ content     │
  │ user_score   │                                     │ answer      │
  │ ai_graded    │                                     │ analysis    │
  └──────┬───────┘                                     └─────────────┘
         │ 作答
         ▼
  ┌──────────────┐
  │exam_qu_answer│
  │              │
  │ user_id (FK) │
  │ exam_id (FK) │
  │ question_id  │
  │ answer_content│──→ 主观题回答
  │ is_right     │──→ 客观题正误
  │ ai_score     │──→ AI评分 (Spearman=0.87)
  │ ai_reason    │──→ AI评语
  │ score        │──→ 最终得分
  └──────────────┘

  知识库相关:
  t_study_material ──→ t_rag_parse_task ──→ t_rag_knowledge
  (资料元数据)          (异步解析任务)        (知识分块)
```

### 8.2 核心数据表

**用户与权限:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_user` | 用户账号 | id, username, password(BCrypt), nickname, role(1学生/2教师/3管理员), grade_id, is_deleted |
| `t_role` | 角色定义 | id, role_name, role_code |

**考试与题目:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_exam` | 考试配置 | id, title, exam_duration, gross_score, passed_score, max_count(切屏上限), start_time, end_time, user_id(创建教师) |
| `t_question` | 题目库 | id, type(1单选/2多选/3判断/4简答), content, answer(标准答案), analysis(解析), repo_id, category_id |
| `t_option` | 题目选项 | id, qu_id(FK), content, is_right, sort |
| `t_exam_question` | 考试-题目关联 | exam_id(FK), question_id(FK), score(本题分值), sort(题号), type |
| `t_exam_repo` | 考试-题库关联 | exam_id, repo_id |
| `t_exam_grade` | 考试-班级关联 | exam_id, grade_id |
| `t_repo` | 题库 | id, title, repo_code |
| `t_category` | 题目分类 | id, name |

**答题与成绩:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_exam_qu_answer` | 学生答案（★核心表） | id, user_id, exam_id, question_id, answer_id(客观题选项), answer_content(主观题), is_right, **ai_score**(AI评分), **ai_reason**(AI评语), score(最终得分) |
| `t_user_exams_score` | 考试成绩总表 | user_id, exam_id, user_score, user_time, whether_mark(是否阅卷), **ai_graded**(AI阅卷标记), state(0进行中/1已完成) |
| `t_manual_score` | 人工评分修正 | exam_qu_answer_id, user_id, score |
| `t_teacher_grading` | 教师阅卷记录 | teacher_id, exam_id, student_id |
| `t_user_book` | 错题本 | user_id, question_id, exam_id |
| `t_exercise_record` | 刷题练习记录 | user_id, repo_id, question_id |

**RAG知识库:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_study_material` | 学习资料元数据 | id, user_id, file_name, original_name, file_path, file_type, file_size, knowledge_type(study/grading), show_in_gallery |
| `t_rag_parse_task` | RAG解析任务 | id, material_id(FK), task_status(0待处理/1处理中/2完成/3失败), progress(0-100), error_message |
| `t_rag_knowledge` | RAG知识分块（★核心表） | id, user_id, material_id(FK), chunk_content(longtext), chunk_index, metadata(JSON), knowledge_type |
| `t_ai_config` | AI模型配置 | id, config_name, api_key, base_url, model, temperature, max_tokens, is_active |

**社交与交流:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_discussion` | 讨论帖 | id, user_id, title, content, visibility(0班级/1全平台), grade_id |
| `t_reply` | 讨论回复 | id, discussion_id(FK), user_id, content |
| `t_like` | 点赞 | id, discussion_id/reply_id, user_id |
| `t_chat_message` | 聊天消息 | id, sender_id, receiver_id, content, type |
| `t_friend_relation` | 好友关系 | user_id, friend_id, status |
| `t_notice` | 系统公告 | id, title, content, user_id |
| `t_notice_grade` | 公告-班级关联 | notice_id, grade_id |
| `t_user_notification` | 用户通知 | user_id, content, is_read |

**其他:**

| 表名 | 用途 | 核心字段 |
|------|------|---------|
| `t_grade` | 班级 | id, grade_name, grade_code, user_id(班主任) |
| `t_user_grade` | 用户-班级关联 | user_id, grade_id |
| `t_certificate` | 证书模板 | id, title, image |
| `t_certificate_user` | 用户证书 | user_id, certificate_id(FK), exam_id, code(证书编号) |
| `t_log` | 操作日志 | id, user_id, operation, ip, create_time |
| `t_user_profile` | 用户画像 | user_id, study_goal, weak_subjects, daily_study_time |
| `t_user_token` | 用户Token余额 | user_id, balance, total_earned, total_consumed |
| `t_token_consume_config` | Token消费配置 | 各功能消耗Token数配置 |
| `t_token_order` | Token充值订单 | user_id, amount, status, alipay_trade_no |
| `t_alipay_config` | 支付宝配置 | app_id, private_key, alipay_public_key |
| `t_user_daily_login_duration` | 用户日登录时长 | user_id, login_date, duration |
| `t_countdown_config` | 倒计时配置 | 备考倒计时参数 |
| `t_daily_task` | 每日任务 | 学习任务管理 |

### 8.3 RAG知识库隔离设计

系统通过 `knowledge_type` 字段实现双知识库隔离：

```
┌───────────────────────────┐     ┌───────────────────────────┐
│  knowledge_type = "study" │     │ knowledge_type = "grading"│
│  (学生学习知识库)          │     │ (教师阅卷参考库)           │
│                           │     │                           │
│  学生上传:                 │     │  教师/管理员上传:          │
│  - 电子教材PDF             │     │  - 评分标准文档            │
│  - 课件PPT/Word            │     │  - 参考答案汇编            │
│  - 扫描版真题              │     │  - 阅卷手册                │
│  - 手写笔记(OCR)           │     │  - 知识点评分细则          │
│                           │     │                           │
│  用途: RAG智能问答         │     │  用途: AI阅卷Prompt增强    │
│        AI对话知识增强      │     │        评分准确性保障      │
│                           │     │                           │
│  接口前缀: /rag            │     │  接口前缀: /grading-rag    │
│  权限: role_student        │     │  权限: role_admin         │
└───────────────────────────┘     └───────────────────────────┘
```

---

## 9. 前后端协同机制

### 9.1 RESTful API通信

```
前端 (Vue 2 + Axios)                    后端 (Spring Boot)
───────────────────                    ──────────────────────

[请求拦截器]
  ├─ 自动携带 Authorization: Bearer {jwt}
  └─ 超时设置 60s (适应AI生成)

                            HTTP/REST
                     ──────────────────→
                                         [VerifyTokenFilter]
                                           ├─ 解析JWT
                                           ├─ 验证签名/过期
                                           └─ 设置SecurityContext

                                         [Controller]
                                           ├─ 参数校验(@Validated)
                                           ├─ 权限检查(@PreAuthorize)
                                           └─ 调用Service

                                         [Service]
                                           ├─ 业务逻辑
                                           └─ 事务管理(@Transactional)

                                         [Mapper]
                                           └─ 数据库操作

                            ←──────────
                    统一响应 Result {code, msg, data}

[响应拦截器]
  ├─ code==1 → 成功，返回data
  ├─ code!=1 → 弹出错误提示
  ├─ 自动从响应头更新Token
  ├─ 401 → 清除Token，跳转登录页
  └─ 403 → 提示"无权限"
```

**统一响应格式:**

```json
{
  "code": 1,           // 1=成功, 0=失败
  "msg": "操作成功",
  "data": {}           // 业务数据
}
```

### 9.2 WebSocket实时通信

```
前端 (utils/websocket.js)                 后端 (Spring WebSocket)
────────────────────────                 ─────────────────────────

连接建立:
  new WebSocket('ws://host:8080/api/websocket?userId=X')
  ├─ 自动重连: 指数退避(最大10次)
  └─ 初始间隔 5s
                                  ←──→   ChatWebSocketHandler
                                           ├─ afterConnectionEstablished()
                                           │   └─ 注册session到用户池
                                           ├─ handleTextMessage()
                                           │   ├─ 解析消息类型
                                           │   ├─ 持久化到 t_chat_message
                                           │   └─ 推送到接收方
                                           └─ afterConnectionClosed()
                                               └─ 清理session

消息发送:
  sendMessage({                           WebSocketSession.sendMessage()
    type: 'CHAT',
    senderId: 1,
    receiverId: 2,
    content: '你好'
  })

消息接收:                                  ┌─ 聊天消息: 推送到接收方前端
  EventBus.$emit('websocket-message')     ├─ 公告通知: 推送到班级学生
                                          └─ 系统消息: 推送到指定用户
```

### 9.3 JWT认证流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                          JWT认证完整流程                              │
│                                                                     │
│  1. 登录                                                            │
│     前端: POST /auth/login {username, password}                     │
│     后端: 验证BCrypt密码 → 生成JWT(30分钟) → 返回Token               │
│     前端: 保存Token到Cookie/Vuex                                    │
│                                                                     │
│  2. 请求认证                                                        │
│     前端拦截器: headers.Authorization = "Bearer " + token           │
│     后端过滤器: VerifyTokenFilter 解析JWT → 设置SecurityContext     │
│                                                                     │
│  3. Token刷新                                                       │
│     响应拦截器: 检查响应头 Authorization                             │
│     如果快过期(<15分钟) → 后端自动签发新Token → 前端自动更新         │
│                                                                     │
│  4. 登出                                                            │
│     前端: 清除Token/Roles/WebSocket                                 │
│     后端: 返回登出成功                                              │
│                                                                     │
│  JWT payload:                                                       │
│  {                                                                  │
│    "userId": 1,                                                     │
│    "username": "student",                                           │
│    "roles": ["role_student"],                                       │
│    "exp": 当前时间 + 30分钟                                         │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

### 9.4 文件上传与进度反馈

```
前端 (FileUpload组件)                     后端
───────────────────                     ──────

1. 选择文件                              StudyMaterialController
   ├─ 校验类型(pdf/doc/xls/图片)           └─ upload()
   ├─ 校验大小(<200MB)                        ├─ UUID重命名
   └─ 显示上传进度                             ├─ 写入存储(本地/MinIO/OSS)
                                              └─ 返回 materialId

2. 触发RAG解析
   POST /rag/parse/{materialId}
   └─ 返回 taskId

3. 轮询解析进度 (可取消)
   GET /rag/task/{taskId}                  RagController
   └─ 返回 {status, progress%}             └─ task/{taskId}
                                              ├─ 状态: 待处理/处理中/完成/失败
                                              └─ 进度: 0-100%
4. 解析完成
   └─ 刷新资料列表
   └─ 显示解析内容块
```

### 9.5 路由权限控制流程

```
用户访问页面
      │
      ▼
┌──────────────────┐
│ router.beforeEach│  (permission.js)
│ 路由前置守卫      │
└────────┬─────────┘
         │
    ┌────▼────┐
    │有Token? │
    └─┬─────┬─┘
      │是   │否
      ▼     ▼
┌──────────┐  ┌──────────┐
│访问/login?│  │白名单路由?│
└─┬─────┬──┘  └─┬─────┬──┘
  │是   │否     │是   │否
  ▼     ▼       ▼     ▼
 重定向  │     放行  跳转/login
 到首页  ▼
    ┌──────────┐
    │有用户信息?│
    └─┬─────┬──┘
      │否   │是
      ▼     ▼
   dispatch  │
   getInfo   │
      │      │
      ▼      ▼
  ┌──────────────────┐
  │学生首次登录?      │
  │→ 跳转备考问卷    │
  └──────────────────┘
         │
         ▼
   NProgress.done()
   放行路由
```

---

## 10. 快速开始

### 10.1 环境要求

| 依赖 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 11 | 后端运行环境，需配置 `--add-opens` (Java 9+) |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 14.x+ | 前端运行环境 |
| npm | 6.14+ | 前端包管理 |
| MySQL | 8.0 | 业务数据存储 |
| Redis | 6.0+ | 缓存与会话存储 |
| Milvus | 2.x | 向量数据库 (RAG必需) |
| Tesseract-OCR | 5.8.0 | OCR识别 (安装到 C:\Program Files\Tesseract-OCR\) |

### 10.2 后端启动

```bash
# 1. 进入后端项目目录
cd online-exam-system-backend-master

# 2. 修改配置文件
# 编辑 src/main/resources/application-dev.yml
#   - 修改 MySQL 连接信息 (url/username/password)
#   - 修改 Redis 连接信息
#   - 修改 Milvus 连接信息
#   - 修改 ai-custom.api-key (阿里百炼DashScope API Key)

# 3. 创建数据库并导入初始数据
mysql -u root -p
CREATE DATABASE db_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
USE db_exam;
SOURCE sql/db_exam.sql;
# 依次执行sql/目录下的其他迁移脚本

# 4. 编译启动
mvn clean install -DskipTests
mvn spring-boot:run

# 后端启动成功: http://localhost:8080/api
# API文档: http://localhost:8080/api/doc.html
```

### 10.3 前端启动

```bash
# 1. 进入前端项目目录
cd online-exam-system-frontend-master

# 2. 安装依赖
npm install
# 如遇网络问题: npm install --registry=https://registry.npmmirror.com

# 3. 配置后端地址
# 编辑 .env.development
# VUE_APP_BASE_API = 'http://localhost:8080/api'

# 4. 启动开发服务器
npm run dev

# 前端访问: http://localhost:9528

# 5. 生产构建
npm run build:prod
# 产物在 dist/ 目录
```

### 10.4 JVM启动参数 (Java 11+)

```bash
java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.security=ALL-UNNAMED \
  --add-opens java.base/sun.net.www.protocol=ALL-UNNAMED \
  --add-exports java.base/sun.net.www.protocol=ALL-UNNAMED \
  --illegal-access=permit \
  -jar exam-1.0-SNAPSHOT.jar
```

---

## 11. 配置说明

### 11.1 后端核心配置 (application-dev.yml)

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 后端服务端口 |
| `server.servlet.context-path` | /api | API上下文路径 |
| `jwt.secret` | (256位密钥) | JWT签名密钥 |
| `jwt.expiration` | 1800000 | Token过期时间(30分钟) |
| `jwt.refreshThreshold` | 900000 | Token刷新阈值(15分钟) |
| `online-exam.storage.type` | local | 文件存储方式: local/minio/aliyun |
| `online-exam.login.captcha.enabled` | true | 是否启用验证码 |
| `online-exam.auto-scoring.enabled` | true | 是否启用AI自动阅卷 |
| `system.register.enabled` | true | 是否开放注册 |
| `ai-custom.enabled` | true | 是否启用AI功能 |
| `ai-custom.api-key` | sk-xxx | 阿里百炼API Key |
| `ai-custom.base-url` | dashscope.aliyuncs.com | AI服务地址 |
| `ai-custom.model` | qwen3.5-122b-a10b | 使用的大模型 |
| `ai-custom.temperature` | 0.7 | 生成温度(对话0.7, 阅卷0.3) |
| `ai-custom.max-tokens` | 2000 | 最大生成Token数 |
| `milvus.host` | 192.168.49.130 | Milvus向量数据库地址 |
| `milvus.port` | 19530 | Milvus端口 |
| `file.upload-dir` | ./uploads | 本地文件上传目录 |
| `file.max-size` | 104857600 | 文件大小限制(100MB) |
| `spring.datasource.url` | jdbc:mysql://127.0.0.1:3306/db_exam | MySQL连接 |
| `spring.redis.host` | 127.0.0.1 | Redis地址 |
| `spring.servlet.multipart.max-file-size` | 100MB | 上传文件最大 |
| `server.tomcat.connection-timeout` | 1800000 | 连接超时30分钟(适应OCR) |

### 11.2 前端环境变量配置 (.env.*)

| 变量 | 说明 |
|------|------|
| `VUE_APP_BASE_API` | 后端API基础地址 |
| `VUE_APP_ENABLE_REGISTER` | 是否显示注册入口 |
| `VUE_APP_CAPTCHA_ENABLED` | 是否启用验证码 |
| `VUE_APP_TITLE` | 网站标题 |
| `VUE_APP_ICP_NUMBER` | 备案号 |

---

## 12. API接口总览

### 12.1 接口规范

- 基础路径: `/api`
- 认证方式: `Authorization: Bearer <jwt_token>`
- 响应格式: `{ code: 1成功/0失败, msg: "消息", data: {...} }`
- 权限控制: `@PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")`

### 12.2 主要接口一览

| 模块 | 前缀 | 主要接口 | 权限 |
|------|------|---------|------|
| **认证** | `/auth` | POST login, POST register, POST captcha | 公开 |
| **用户** | `/user` | GET paging, PUT update, GET info | 登录用户 |
| **考试** | `/exam` | GET paging, POST add, GET detail/{id}, GET start/{id}, GET hand/{id}, PUT cheat/{id} | 学生/教师 |
| **题目** | `/question` | GET paging, POST add, PUT update, DELETE delete, POST excel/import | 教师/管理员 |
| **题库** | `/repo` | GET paging, POST add, GET list | 教师/管理员 |
| **答卷** | `/answers` | GET detail, PUT correct, GET exam/page | 教师 |
| **考试记录** | `/records` | GET exam/paging, GET exam/detail | 学生 |
| **成绩** | `/score` | GET paging, GET question/{examId}/{questionId}, GET export | 学生/教师 |
| **★ AI阅卷** | `/real-ai-grading` | POST grade/{examId}, POST batch-grade/{examId}, GET progress/{examId} | 教师/管理员 |
| **AI阅卷结果** | `/ai-grading/result` | GET exam/{examId}, GET list | 学生/教师 |
| **★ RAG** | `/rag` | POST parse/{materialId}, GET task/{taskId}, GET stats, POST test, DELETE clear | 学生 |
| **阅卷RAG** | `/grading-rag` | POST parse, GET list, DELETE delete | 管理员 |
| **AI对话** | `/ai-chat` | POST chat, GET history | 学生/教师 |
| **AI配置** | `/ai-config` | GET list, POST save, PUT activate/{id} | 管理员 |
| **学习资料** | `/study-material` | POST upload, GET list, GET gallery, DELETE {id} | 学生/教师 |
| **班级** | `/grades` | GET paging, POST add, GET list | 教师/管理员 |
| **讨论** | `/discussion` | GET paging, POST add, POST like | 登录用户 |
| **聊天** | `/chat-message` | GET history, WebSocket 实时通信 | 登录用户 |
| **公告** | `/notice` | GET paging, POST add | 教师/管理员 |
| **证书** | `/certificate` | GET paging, GET my | 登录用户 |
| **错题本** | `/userbook` | GET paging, POST add | 学生 |
| **日志** | `/log` | GET paging | 管理员 |
| **首页** | `/home` | GET stats | 登录用户 |
| **个性化** | `/personalized-home` | GET dashboard | 学生 |
| **用户画像** | `/user-profile` | GET, PUT update | 登录用户 |
| **用户问卷** | `/user-questionnaire` | POST submit, GET status | 学生 |
| **Token** | `/user-token` | GET balance, POST purchase | 学生/管理员 |

---

## 13. 部署指南

### 13.1 Docker Compose 一键部署

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: db_exam
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./online-exam-system-backend-master/sql/db_exam.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:6
    ports:
      - "6379:6379"

  milvus:
    image: milvusdb/milvus:v2.3.3
    ports:
      - "19530:19530"
    volumes:
      - milvus_data:/var/lib/milvus

  backend:
    build: ./online-exam-system-backend-master
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - milvus
    environment:
      SPRING_PROFILES_ACTIVE: prod

  frontend:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./online-exam-system-frontend-master/dist:/usr/share/nginx/html
    depends_on:
      - backend
volumes:
  mysql_data:
  milvus_data:
```

### 13.2 Nginx反向代理配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;

        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # 大文件上传 & AI长连接
        client_max_body_size 100m;
        proxy_read_timeout 1800s;
    }
}
```

---

## 14. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | teacher | teacher123 |
| 学生 | student | student123 |

> **注意**: 以上为数据库初始化SQL中的默认测试账号，首次部署后请及时修改密码。

---

## 15. 隐私配置清单（必读）

本项目已移除所有敏感信息，部署前**必须**填写以下配置项：

### 15.1 后端配置 (`application-dev.yml` / `application-prod.yml`)

| 配置项 | 占位符 | 说明 | 获取方式 |
|--------|--------|------|---------|
| `jwt.secret` | `YOUR_JWT_SECRET_HERE_AT_LEAST_256_BITS` | JWT签名密钥（至少256位） | 自行生成随机字符串 |
| `spring.datasource.password` | `YOUR_MYSQL_PASSWORD_HERE` | MySQL数据库密码 | 你的MySQL密码 |
| `spring.redis.password` | `YOUR_REDIS_PASSWORD_HERE` | Redis密码（prod环境） | 你的Redis密码 |
| `oss.access-key-id` | `YOUR_OSS_ACCESS_KEY_ID_HERE` | 阿里云OSS AccessKey ID | [阿里云控制台](https://ram.console.aliyun.com) |
| `oss.access-key-secret` | `YOUR_OSS_ACCESS_KEY_SECRET_HERE` | 阿里云OSS AccessKey Secret | [阿里云控制台](https://ram.console.aliyun.com) |
| `minio.accesskey` | `YOUR_MINIO_ACCESS_KEY_HERE` | MinIO用户名 | MinIO控制台设置 |
| `minio.secretKey` | `YOUR_MINIO_SECRET_KEY_HERE` | MinIO密码 | MinIO控制台设置 |
| `milvus.password` | `YOUR_MILVUS_PASSWORD_HERE` | Milvus向量数据库密码 | Milvus部署时设置 |
| `milvus.host` | `127.0.0.1` | Milvus服务地址 | 按实际部署修改 |
| `ai-custom.api-key` | `YOUR_AI_API_KEY_HERE` | 阿里百炼DashScope API Key | [阿里百炼控制台](https://bailian.console.aliyun.com) |

### 15.2 主配置 (`application.yml`)

| 配置项 | 占位符 | 说明 |
|--------|--------|------|
| `spring.ai.api-key` | `YOUR_AI_API_KEY_HERE` | AI服务API Key |
| `jwt.secret` | `YOUR_JWT_SECRET_HERE_AT_LEAST_256_BITS` | JWT签名密钥 |

### 15.3 前端加密配置

| 文件 | 配置项 | 占位符 | 说明 |
|------|--------|--------|------|
| `src/utils/Secret.js` | `KEY` / `IV` | `YOUR_AES_KEY_HERE` / `YOUR_AES_IV_HERE` | 前端AES加密密钥和IV（16位字符串） |

### 15.4 后端加密配置

| 文件 | 配置项 | 占位符 | 说明 |
|------|--------|--------|------|
| `cn.org.alan.exam.utils.SecretUtils` | `KEY` / `IV` | `YOUR_AES_KEY_HERE` / `YOUR_AES_IV_HERE` | 后端AES加密密钥和IV（16位字符串，需与前端一致） |

### 15.5 环境变量方式（推荐）

所有带 `${ENV_VAR:default}` 格式的配置项支持通过环境变量覆盖，例如：

```bash
# Linux/macOS
export AI_API_KEY=sk-your-actual-api-key
export JWT_SECRET=your-generated-secret
export MYSQL_PASSWORD=your-mysql-password

# Windows PowerShell
$env:AI_API_KEY = "sk-your-actual-api-key"
$env:JWT_SECRET = "your-generated-secret"
$env:MYSQL_PASSWORD = "your-mysql-password"
```

### 15.6 生成JWT密钥示例

```bash
# 使用openssl生成256位随机密钥
openssl rand -base64 64

# 或使用Java
java -e 'import java.util.UUID; print(UUID.randomUUID().toString().replace("-","") + UUID.randomUUID().toString().replace("-",""))'
```

---

## 项目文件清单

| 文件 | 说明 |
|------|------|
| `online-exam-system-backend-master/` | Spring Boot 后端项目 (42个Controller, 56个Service, 49个Mapper) |
| `online-exam-system-frontend-master/` | Vue 2 前端项目 (46个页面, 41个API模块, 6个Vuex模块) |
| `sql/` | 数据库初始化与增量迁移SQL (26个脚本) |
| `pom.xml` | Maven依赖配置 (50+ 直接依赖) |
| `package.json` | NPM依赖配置 (30+ 依赖) |

---

**技术栈总结**: Java 11 + Spring Boot 2.1.3 + MyBatis Plus 3.5.5 + MySQL 8.0 + Redis + Milvus + LangChain4j 0.29.1 + DashScope + PDFBox + Tesseract OCR 5.8 + Apache POI + Vue 2.6 + Element UI 2.13 + ECharts 4.9 + WebSocket

**测试数据**: AI阅卷与人工评分Spearman系数0.87 | OCR预处理加速11.3倍 | 100并发RAG问答 <10秒 | 图像预处理准确率提升13.8%