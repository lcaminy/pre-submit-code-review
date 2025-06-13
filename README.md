# Pre-Submit Code Review Plugin

IntelliJ IDEA 插件，用于在代码提交前进行 AI 辅助的代码审查。

## 功能特性

- 代码提交前自动审查
- 基于 AI 的代码质量分析
- 文件缓存和性能优化
- 代码脱敏保护
- 自定义规则配置

## 构建要求

- JDK 17+
- Gradle 7.6+
- IntelliJ IDEA 2023.1+

## 开发环境设置

1. 克隆仓库：
```bash
git clone https://github.com/your-org/pre-submit-code-review.git
cd pre-submit-code-review
```

2. 导入项目：
- 使用 IntelliJ IDEA 打开项目
- 等待 Gradle 同步完成

3. 运行插件：
```bash
./gradlew runIde
```

## 构建发布

### 本地构建

```bash
./gradlew clean build
```

构建的插件包位于 `build/distributions/` 目录。

### 自动化发布

1. 设置环境变量：
- 在 GitHub 仓库设置中添加 `INTELLIJ_PUBLISH_TOKEN` secret

2. 创建新版本：
```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions 将自动：
- 构建插件
- 创建 GitHub Release
- 发布到 JetBrains Marketplace

## 性能测试

运行性能测试套件：
```bash
./gradlew test --tests "com.renrui.review.performance.PerformanceTest"
```

## 配置说明

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
```

### 插件配置

在 IDE 设置中配置：
- API 密钥
- 审查规则
- 缓存设置
- 超时阈值

## 许可证

Apache License 2.0 