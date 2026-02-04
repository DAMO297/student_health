# 个人中心功能 - 404 错误排查

## 问题

访问 `/api/user/profile` 返回 404 错误，Spring 无法找到 UserController 的映射。

## 原因

新创建的 `UserController` 需要重新编译才能被 Spring Boot 扫描和加载。

## 解决方法

### 方法1：IDEA 重新构建（推荐）

1. 在 IDEA 中，点击菜单：**Build** → **Rebuild Project**
2. 等待编译完成
3. 重启 Spring Boot 应用

### 方法2：Maven 重新编译

```bash
# 在项目根目录执行
mvn clean compile

# 或者重新打包
mvn clean package
```

### 方法3：手动重启

1. 停止当前运行的 Spring Boot 应用
2. 清理编译输出目录 `target/`
3. 重新运行应用

## 验证

重启后，检查日志中是否有类似输出：
```
Mapped "{[/api/user/profile],methods=[GET]}" onto org.example.user.UserController.getProfile()
```

或者使用浏览器/Postman访问：
- `GET /api/user/profile` （需要登录token）

## 已实现的端点

- `GET /api/user/profile` - 获取当前用户信息
- `PUT /api/user/profile` - 更新个人资料
- `PUT /api/user/change-password` - 修改密码

## 前端页面

- 路由：`/profile`
- 侧边栏菜单：点击"个人中心"
- 顶部导航：点击用户名或头像
