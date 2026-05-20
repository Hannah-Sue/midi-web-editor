# Web MIDI 编辑器

基于浏览器的 MIDI 钢琴卷帘编辑器，支持多轨编辑、试听、导入/导出 MIDI 与 MP3，并集成 DeepSeek 音乐助手辅助作曲。

## 功能概览

- 钢琴卷帘：添加 / 移动 / 删除音符，网格吸附，BPM 与多轨音量
- 导入 / 导出标准 MIDI、导出 MP3
- 撤销、浏览器内保存与工程 JSON 另存为
- **音乐助手（Agent）**：对话生成音符或整段 MIDI，可载入卷帘继续编辑

## 环境要求

- Node.js 18+
- JDK 17+
- Maven（或使用 `backend/mvnw`）

## 快速启动

### 1. 前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 Vite 提示的地址（通常 http://localhost:5173）。

### 2. 后端（Agent 功能需要）

```bash
# 配置 DeepSeek Key（勿写入仓库）
# PowerShell:
$env:DEEPSEEK_API_KEY="你的密钥"

cd backend
./mvnw spring-boot:run
```

后端默认端口 **8000**，前端通过 Vite 代理访问 `/api`。

### 3. 验证 Agent

访问 http://localhost:8000/api/agent/status ，`configured` 为 `true` 表示 Key 已生效。

更多说明见 [docs/Agent助手使用说明.md](docs/Agent助手使用说明.md)。

## 项目结构

```
web-midi-editor/
├── frontend/          # Vue 3 + Vite + Tone.js
├── backend/           # Spring Boot，Agent API 代理 DeepSeek
└── docs/              # 需求、架构与使用文档
```

## 比赛 / 评审说明

- **无需 Key 也可体验**：不启动后端时，编辑器核心功能（卷帘、播放、导入导出）仍可使用；助手需自备 DeepSeek API Key 与账户余额。
- 演示 Agent 时请先启动后端并配置 `DEEPSEEK_API_KEY`。
- 详细需求与实现对照见 [docs/midi编辑器需求文档.md](docs/midi编辑器需求文档.md)。

## 许可证

参赛提交请以比赛方要求为准。
