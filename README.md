# PetHub · 宠物综合服务平台

一个集 **宠物交易、社区交流、AI 智能咨询** 于一体的全栈宠物平台后端,基于 Spring Boot 3 + Spring AI 构建。
平台围绕"养宠人"这一核心场景,打通了 **C 端用户(浏览/下单/发帖/AI 咨询)** 与 **B 端管理(审核/运营/数据看板)** 两套完整业务闭环。

> 个人独立设计与开发的全栈练手项目,重点投入在 **AI 应用工程化(RAG / 多模态 / 工具调用 / 流式输出)** 与 **高并发场景下的缓存与一致性设计**。

---

## 一、技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 17 |
| Web 框架 | Spring Boot 3.5.6 / Spring MVC |
| 持久层 | MyBatis-Plus 3.5.12 + MySQL 8 |
| 缓存 | Redis(Lettuce 连接池) + Lua 脚本 + Spring Cache |
| **AI 框架** | **Spring AI 1.0.3** |
| AI 模型服务 | 通义千问 Qwen(Chat / Vision / Embedding,OpenAI 兼容模式) |
| 向量检索 | SimpleVectorStore(RAG 知识库) |
| 安全 / 鉴权 | JWT(自定义双拦截器) |
| 文件存储 | 阿里云 OSS |
| 实时推送 | SSE(Server-Sent Events,AI 流式回复) |
| 定时任务 | Spring Scheduling |
| 其他 | AOP、Lombok、Jackson、CORS 动态配置 |

## 二、项目结构

```
pet-plotform
├── pet-common      // 通用模块:统一返回、异常、上下文、缓存名常量
├── pet-pojo        // 数据对象:Entity / DTO / VO / Query
└── pet-server      // 业务模块
    └── com.pethub
        ├── aspect          // AOP:公共字段自动填充
        ├── config          // RAG / Cache / Cors / WebMvc 配置
        ├── controller      // admin / user 双端路由
        ├── interceptor     // JWT 鉴权拦截器
        ├── mapper          // MyBatis-Plus Mapper
        ├── service         // 业务服务(含 AiAssistantService、SpringAiChatMemory)
        ├── task            // 订单超时取消 / 自动完成定时任务
        ├── utils           // JwtUtil、KnowledgeIngestUtil、PetUtill
        └── properties      // JWT / OSS / Qwen / Cors 配置绑定
```

Maven 多模块,common / pojo / server 三层职责清晰,便于复用与后续微服务化。

## 三、核心功能

### 用户端(C 端)
- **认证体系**:JWT 登录、注册、个人资料维护、收货地址管理
- **宠物浏览 / 交易**:分类筛选、宠物详情、下单、订单状态跟踪
- **社区帖子**:发布 / 浏览 / 点赞 / 评论 / 二级回复,管理员审核机制
- **AI 智能咨询**:多会话管理、上下文记忆、流式回复、图片识别、知识库问答、平台数据工具调用

### 管理端(B 端)
- 用户 / 宠物 / 分类 / 订单 / 帖子 / 公告 的 CRUD 与状态管理
- 帖子审核工作流
- **数据看板**:总览统计、订单趋势、品类分布、近期订单 / 帖子(带 Redis 缓存)
- 操作日志与公告推送

## 四、技术亮点

### 1. Spring AI 一体化:RAG + 工具调用 + 多模态 + 流式

`AiAssistantServiceImpl` 是项目最核心的工程产出,整合了 Spring AI 的多项能力:

- **RAG 知识库**
  启动时通过 `KnowledgeIngestUtil` 将 `resources/kb/*.txt`(平台规则、免责声明等)切片 + Embedding 写入 `SimpleVectorStore`;
  对话时挂载 `QuestionAnswerAdvisor`,基于语义相似度召回相关上下文注入 Prompt,实现平台规则的可控应答。
- **Function Calling / Tools**
  当用户问"你们这有没有 XX 宠物"时,模型自动调用 `CategoryService` 查询平台实时数据再作答,而不是凭空回复 —— 解决了 LLM "幻觉"问题。
- **多模态(Vision)**
  用户上传图片 URL → 通过 `Media + MimeType` 注入给 Qwen-VL 模型 → 实现"图片识别 + 文字咨询"混合场景。
- **流式输出(SSE)**
  Controller 返回 `SseEmitter`,Service 用 `Flux<String>` 把 token 增量推给前端,体验接近 ChatGPT。
- **多会话上下文记忆**
  自实现 `SpringAiChatMemory`(基于数据库持久化),替代默认内存 ChatMemory,会话刷新页面不丢上下文。

### 2. Redis 缓存 + Lua 脚本

- **看板数据**:使用 `@Cacheable` + `DashboardCacheNames` 常量集中管理缓存 key,避免散落。
- **分布式锁释放**:`resources/lua/unlock.lua` 用 Lua 保证 "校验 value + 删除 key" 的原子性,避免误删他人锁。
- **缓存一致性**:写操作触发 `@CacheEvict` 精准失效,而非全量清理。

### 3. AOP 公共字段自动填充

`AutoFillTimeAspect` + 自定义注解,在 Insert / Update 时统一填充 `createTime / updateTime / createUser / updateUser`,消除模板代码。

### 4. JWT 双拦截器 + 上下文透传

管理端、用户端独立 JWT 密钥与拦截路径;解析后将用户 ID 写入 `BaseContext`(ThreadLocal),Service 层直接取用,避免参数透传。

### 5. 订单状态机 + 定时任务

- `ordertask.cancelPayOrder` 每分钟扫描超时 5 分钟未支付订单 → 自动取消
- `ordertask.complishOrder` 每天凌晨 1 点扫描派送中订单 → 自动完成
- 状态变更通过 `OrderStatusLog` 留痕,便于审计与售后纠纷

### 6. 配置安全

敏感信息(数据库密码、Qwen API Key、OSS Secret)统一外置到环境变量;`pethub` 命名空间下的 `*Properties` 类通过 `@ConfigurationProperties` 强类型绑定。

### 7. CORS 动态配置

`CorsProperties` 支持运行时调整允许的 Origin / Header / Method,无需重启即可对接新前端。

## 五、本地运行

### 环境要求
- JDK 17+
- MySQL 8.x
- Redis 6.x
- Maven 3.8+
- 通义千问 API Key([申请地址](https://dashscope.aliyun.com))

### 启动步骤

```bash
# 1. 导入数据库脚本
#    pet-server/src/main/resources/sql/pet_platform_init.sql
#    pet-server/src/main/resources/sql/post_interaction_extend.sql

# 2. 配置环境变量(或修改 application-dev.yaml)
export QWEN_API_KEY=sk-xxxxxx
export QWEN_MODEL=qwen-plus
export QWEN_VISION_MODEL=qwen-vl-plus
export QWEN_EMBEDDING_MODEL=text-embedding-v3

# 3. 编译
mvn clean package -DskipTests

# 4. 启动
java -jar pet-server/target/pet-server-0.0.1-SNAPSHOT.jar
```

启动后服务监听 `http://localhost:9090`。

## 六、后续规划(Roadmap)

- [ ] 接入 RabbitMQ / RocketMQ 解耦订单与通知
- [ ] 向量库由 `SimpleVectorStore` 切换为 Milvus / Redis Vector,支持持久化与大规模检索
- [ ] AI 助手增加 Agent 多步推理(Spring AI Tools 链式调用)
- [ ] 引入 SkyWalking / Prometheus 做链路追踪与指标监控
- [ ] 拆分微服务(用户中心 / 订单中心 / AI 网关)
- [ ] 补充单元测试 + GitHub Actions CI

---

> 本项目仅用于学习与技术演示,不用于商业用途。
