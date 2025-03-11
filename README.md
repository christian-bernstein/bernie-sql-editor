![Banner](https://github.com/christian-bernstein/sql-editor-web/blob/48031558c42cdb084b49631138a75a8179821afb/sql-editor-banner.png?raw=true)

# SQL Editor

This repository contains SQL-Editor's backend source. 
<br>
> [!CAUTION]
> **SQL-Editor is not meant for public usage. It contains critical security vulnerabilities:**
> E.g. passwords are stored in plaintext

<br>
<br>

## Philosophy & Motivation
I started the SQL-Editor project in mid 2021, based on an earlier project from 2019. 
The front and backend were parts of an optional project I made for my high school graduation in 2022.

The objective was to create an easy-to-use, web-based environment for students at my school to learn and get comfortable 
writing SQL and interacting with real databases. For me, it was a great opportunity to start diving into web development.

## Library introductions
- [Discovery: Std. WebSocket Impl.](bernie-sql-editor-sdk/src/main/java/de/christianbernstein/bernie/sdk/discovery/websocket/info_and_examples/intro.md): packet-based WebSocket communication system used throughout the SQL-Editor backend

## Basic file structure
**Sub Projects**
- [`bernie-sql-editor-sdk`](bernie-sql-editor-sdk/): Small (partly standalone) libraries and miscellaneous classes I developed for SQL-Editor
- [`bernie-sql-editor-ton`](bernie-sql-editor-ton/): Actual implementation of SQL-Editor backend

**Noteworthy directories & files**
- [Main class](bernie-sql-editor-ton/src/main/java/de/christianbernstein/bernie/ses/bin/TonLauncher.java): Main class that starts the backend
- [Frontend communication link](bernie-sql-editor-sdk/src/main/java/de/christianbernstein/bernie/sdk/discovery/): Abstract, packet-based communication layer 

## Updates & Issues
This project is archived. I won't continue adding new features or resolve security vulnerabilities and bugs.
