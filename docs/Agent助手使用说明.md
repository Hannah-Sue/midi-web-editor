# Agent 音乐助手使用说明

## 功能

- 右侧 **音乐助手** 面板，基于 DeepSeek 模型（经 Java 后端代理，API Key 不暴露给浏览器）
- 多会话管理：新建、切换、删除对话，历史保存在浏览器 `localStorage`
- 乐理咨询 + 自动生成 MIDI 音符
- 生成结果可通过 **添加音符** / **载入完整 MIDI** 写入钢琴卷帘，之后可手动编辑

## 启动

1. 配置 API Key（[DeepSeek 开放平台](https://platform.deepseek.com/) 申请）：
(后端读取的是环境变量 DEEPSEEK_API_KEY，或 application.properties 里的 deepseek.api-key)
   ```bash
   # Windows PowerShell
   $env:DEEPSEEK_API_KEY="sk-..."
   ```

2. 启动后端（端口 8000）：

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. 启动前端：

   ```bash
   cd frontend
   npm run dev
   ```

   Vite 已将 `/api` 代理到 `http://localhost:8000`。

## 使用示例

- 「用 C 大调写一段 8 小节的钢琴旋律，节奏轻快」
- 「在当前音轨追加一个 C 大三和弦，从第 2 秒开始」
- 「把 BPM 改成 90 并解释 4/4 拍下的八分音符时值」

助手回复中的 JSON 块会被解析；若包含 `append_notes` 或 `replace_project`，消息下方会出现应用按钮。

## 常见错误

| 现象 | 原因 | 处理 |
|------|------|------|
| `Insufficient Balance` / `402 Payment Required` | DeepSeek 账户**余额不足** | 登录 [DeepSeek 开放平台](https://platform.deepseek.com) → 充值 / 开通计费，确保账户有余额 |
| `未配置 DeepSeek API Key` | 后端未读到 `DEEPSEEK_API_KEY` | 按上文配置环境变量后**重启后端** |
| `401` / `invalid_api_key` | Key 错误或已删除 | 在平台重新创建 Key 并更新环境变量 |

说明：出现 402 通常表示 **Key 已生效**，只是该账号没有可用额度，与项目代码无关。
