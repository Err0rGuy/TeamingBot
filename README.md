# TeamingBot 🤖

A lightweight Telegram bot built with **Spring Boot**, **Java Telegram Bots API**, and **MySQL**, designed to organize and coordinate team members in group chats.

## Features

- 🧩 **Team Management** — Create and manage teams (e.g. `backend`, `frontend`, etc.) within a Telegram group.  
- 🔔 **Team Mentions** — Mention a team using `#teamname` and automatically notify all members privately.  
- 🔗 **Message Linking** — Each private notification includes a direct link to the original group message.  
- ⚙️ **Persistent Storage** — Uses MySQL for storing team and user data.  
- 🔐 **Built with Spring Boot** — Clean architecture and easy configuration via `.env` or environment variables.

## Tech Stack

- **Spring Boot**
- **TelegramBots Java API**
- **MySQL**

## Configuration

Set the following environment variables in a `.env` file or through your environment:

```bash
# DataBase
DB_URL=jdbc:mysql://localhost:3306/botDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=<UserName>
DB_PASSWORD=<Password>

# Telegram Bot
BOT_TOKEN=<BotToken>
BOT_USERNAME=<BotUserName>
PROXY_HOST=
PROXY_PORT=
PROXY_TYPE=

# SpringBoot
SERVER_PORT=8080