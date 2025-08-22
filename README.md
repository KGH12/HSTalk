<div align="center">

# 🎈 H&S Talk 🎈

</div>

<p align="center">
  <strong>수업 내용을 바탕으로 개발한 Java 기반의 멀티유저 채팅 애플리케이션입니다.</strong><br>
  카카오톡의 핵심 기능을 구현하여 실시간 메시지, 이모티콘, 이미지 전송이 가능합니다.
</p>

<p align="center">
  <img src="https://i.imgur.com/your-main-image.gif" alt="project-gif" width="700"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java"/>
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen?style=for-the-badge"/>
</p>

---

## ✨ 주요 기능

* **👨‍💻 로그인**: 간단히 이름만 입력하여 채팅 서버에 접속할 수 있습니다.
* **🏡 채팅방 목록**: 생성된 채팅방 목록을 확인하고, 새로운 채팅방을 직접 만들 수 있습니다.
* **💬 실시간 채팅**:
    * 메시지를 보내면 보낸 시간과 이름이 함께 표시됩니다.
    * 텍스트뿐만 아니라 이모티콘과 이미지를 전송할 수 있습니다.
* **🤫 귓속말**: `/w [이름] [메시지]` 명령어로 특정 사용자에게만 비밀 메시지를 보낼 수 있습니다.
* **👥 참여자 확인**: `/list` 명령어로 현재 채팅방에 있는 모든 사용자를 확인할 수 있습니다.
* **✍️ 대화 로그 저장**: 모든 대화는 서버의 `txts` 폴더에 `.txt` 파일로 자동 저장되어 언제든지 다시 볼 수 있습니다.

## 📸 스크린샷

<table>
  <tr>
    <td align="center"><strong>채팅방 생성</strong></td>
    <td align="center"><strong>메시지 및 이모티콘 전송</strong></td>
  </tr>
  <tr>
    <td><img src="https://i.imgur.com/your-screenshot-1.png" alt="채팅방 생성" width="400"/></td>
    <td><img src="https://i.imgur.com/your-screenshot-2.png" alt="메시지 전송" width="400"/></td>
  </tr>
  <tr>
    <td align="center"><strong>귓속말 및 참여자 확인</strong></td>
    <td align="center"><strong>메시지 로그 저장</strong></td>
  </tr>
  <tr>
    <td><img src="https://i.imgur.com/your-screenshot-3.png" alt="부가 기능" width="400"/></td>
    <td><img src="https://i.imgur.com/your-screenshot-4.png" alt="로그 저장" width="400"/></td>
  </tr>
</table>

## 🚀 실행 방법

1.  **서버(Server)를 실행합니다.**
    ```java
    // Server.java 실행
    ```
    GUI 창이 나타나면 `서버 시작` 버튼을 클릭하여 포트 `12345`에서 서버를 활성화합니다.

2.  **클라이언트(Client)를 실행합니다.**
    ```java
    // Client.java 실행
    ```
    여러 사용자로 접속하려면, `Client.java`를 여러 번 실행해 주세요.

## 📂 프로젝트 구조

```
H&S-Talk/
├── src/
│   └── package1/
│       ├── Server.java
│       ├── Client.java
│       ├── Login.java
│       ├── ChatRoomList.java
│       ├── ChattingRoom.java
│       ├── ChatRoomInfo.java
│       └── Function.java
├── data/
├── emoticon/
├── font/
├── images/
└── txts/
```

## 🧑‍💻 기여자

| 이름 | 기여도 |
| :--: | :--: |
| **김경훈** | 50% |
| **최민수** | 50% |


---
