# shinxBot

一个基于[go-cqhttp](https://github.com/Mrs4s/go-cqhttp)的机器人。

### 它能做什么？

- 在QQ群里玩UNO游戏。
- 在群或私聊里玩猜数字游戏。
- 模拟PokémonCaféReMix的外送。（数据未更新）
- 在群或私聊中生成一个假的转发信息。
- 生成一张 水布想要 的图片
- 恶臭数字论证器，来源：[itorr](https://github.com/itorr/homo)
- 来点二次元，似乎JSON API已失效。来源：[dmoe](https://www.dmoe.cc)
- 关键词回复
- 群成员变动提醒
- 在线编译代码
- 数织游戏：手写识别
- 来点色图：随机色卡
- 首字母缩写识别，来源：[itorr](https://github.com/itorr/nbnhhsh)

### 关于源文件

main/Main.java 为入口。

- src/main：包含Main.java和其他文件，用于给go-cqhttp分发消息。
- src/function：包含所有用于处理QQ消息的java文件。
- src/event：包含所有用于处理QQ事件的java文件。(event事件传入整个JSON消息)
- src/httpconnect：用于通过HTTP与go-cqhttp连接和访问其他API的文件
- src/utils：用于下载图片的文件。
- lib：jar库。包含：支持JSON (Alibaba.fastjson)

### 如何运行这个机器人

1. 配置并启动go-cqhttp。打开 "反向HTTP POST地址"，并设置你自己的接收端口。
2. 下载所有文件并运行main.Main（推荐），或者进入发布页面，下载并运行 bot_cqhttp.jar（未更新）。第一次启动需要send_port和receive_port来连接到go-cqhttp。
3. 在这之后，希望它能跑起来。

### 如何添加新功能

1. 在function下创建java文件并implement一个接口文件src/interfaces/Processable

    或者在event下创建java文件并implement一个接口文件src/interfaces/EventProcessable
2. 写好check和process方法。check返回true的时候就会执行process方法。
3. 写好help方法，为功能使用指引。
4. 在main.Main.main()函数中加入 features/events.add(new yourClassName());就行了。

## English version

A bot based on [go-cqhttp](https://github.com/Mrs4s/go-cqhttp)

### What can it do?

- Play UNO game in QQ groups.
- Play a guess number game in group or private.
- Simulate the delivery system of PokémonCaféReMix. (not updated)
- Generate a fake forward message in a group or private.
- Generate a Vaporeon image.
- Homo number generator. From [itorr](https://github.com/itorr/homo).
- Get Japanese anime image, it seems that json API has failed. From [dmoe](https://www.dmoe.cc).
- Keywords reply.
- React to group member change.
- Online compiler.
- Nonogram with handwriting.
- Random color picture.
- Acronym identification. From [itorr](https://github.com/itorr/nbnhhsh).

### About the source files

The main/Main.java is the starting point.

- src/main: contains main.java and other files to distribute message with go-cqhttp.
- src/function: contains all java files to process the QQ Message.
- src/event: contains all java files to process the QQ events. (Will send entire JSON message as parameter)
- src/httpconnect: files used to connect with go-cqhttp or other APIs through HTTP.
- src/utils: files used to download pictures via http.
- lib: libraries. Contains: support files for JSON. (Alibaba.fastjson)

### How to run the bot

1. Configure and start go-cqhttp. Do open the "反向HTTP POST地址", and set your own reception port.
2. download all the files and run main.Main (recommended), or Go to Release Page, download and run the bot_cqhttp.jar (not up to date). It will require send_port and receive_port at the first time to connect to go-cqhttp
3. After this it is expected to run.

### How to add new features

1. Create a java file under function directory and implement an interface file src/interfaces/Processable

   Or create under event and implement an interface file src/interfaces/EventProcessable
2. Write check and process methods. The process method will be executed when check returns true.
3. Write help methods to guide the use of the function.
4. Add features/events.add(new yourClassName()); in the main.Main.main() Method.
