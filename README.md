# Real-Time Chat Application

A simple real-time group chat application built with Java Socket Programming.
Multiple clients can connect to one server and exchange messages instantly from the console.

## Features

- Multi-user real-time chat
- Java sockets and threads
- Join and leave notifications
- Username support
- `/users` command to view online users
- `/quit` command to leave chat
- No external dependencies

## Project Structure

```text
src/
  chat/
    ChatServer.java
    ChatClient.java
```

## Requirements

- JDK 11 or newer

Check Java:

```bash
java -version
javac -version
```

## Compile

From the project root:

```bash
javac -d out src/chat/*.java
```

## Run

Start the server first:

```bash
java -cp out chat.ChatServer
```

The server runs on port `5000` by default.

Open another terminal for each user and run:

```bash
java -cp out chat.ChatClient
```

You can also provide host and port:

```bash
java -cp out chat.ChatClient localhost 5000
```

## Chat Commands

```text
/users  Show online users
/quit   Exit the chat
```

## Example

Terminal 1:

```text
java -cp out chat.ChatServer
```

Terminal 2:

```text
java -cp out chat.ChatClient
Enter your username: Rohith
```

Terminal 3:

```text
java -cp out chat.ChatClient
Enter your username: Alex
```

Now both users can chat in real time.
