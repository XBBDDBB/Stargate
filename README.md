# 「星际之门」Stargate
##  旗下分为：「量子跃迁」「记忆锚点」
## 📋 项目介绍
# 「量子跃迁(桥)」QuantumTransitionBridge
#### 什么？要让我把前台应用和后台批量分别使用不同的数据库？？？而且还是一个mysql系一个pg系？？？什么？？？我司产品不支持多数据源？？？那我怎么把批量跑出来的结果，丢给前台应用供其查看啊？？？🤷要不你整死我吧
#### 什么？开发环境、集成测试环境、业务测试环境、准生产环境，你整这么老多环境，你迁移数据是想累死我嘛？什么？？？还不给ssh权限和命令行工具？？？那我怎么把开发环境的数据迁移到其他环境啊？？？什么？你的意思，让我在开发环境导出来insert语句，然后，打tar包，使用ftp工具下载下来，然后本地打开dbeaver等工具，手动执行？？？🤷要不你整死我吧
#### 什么？！@#¥%……&*()_+
#### 啊哈！
#### 现在有了「量子跃迁桥」，将你的不同语法体系的数据库的数据，通过「量子跃迁」的形式，又快又准的进行互相迁移，从你到我，从我到你！
#### 「量子跃迁桥」支持多表按照直接追加、先删除再插入、先创建分区再插入等方式，进行数据传输。我们把这一过程叫做开启量子跃迁桥。
# 「记忆锚点(桩)」MemoryAnchorStake
#### 您在当牛做马时，是否有过，修改存储过程或表结构时，自信的都不知道自己是谁，过了许久爆炸了以后痛苦不已，没有备份，现在十分的后悔！
#### 您在当牛做马时，是否有过，下等马队友动了你最为重要的东西，但是由于自己没有想到有朝一日自己可以比窦娥还冤？现在十分的气愤！！！！
#### 您在当牛做马时，是否有过，什么？运维不给ssh权限？不给命令行工具？无法通过命令行导出全库DDL作为上线脚本，还玩个屁？？？？？？？
#### 您在当牛做马时，是否有过，！@#¥%……&*()_+
#### 啊哈！
#### 现在有了「记忆锚点桩」，将你的「记忆」定时的创建「锚点」，使其钉死在「世界树」上。当你想要寻找「失去」的记忆时，可直接使用「锚点」找回当时的自己！
#### 「记忆锚点桩」支持按照死该马（Schema）、按照Type（表、视图、函数、存储过程）、按照奥不债（Object）名字，分类逐一备份DDL。我们把这一过程叫做生成记忆锚点，在您需要时，可对单一对象进行查看及手动恢复。
#### 支持依赖过往「记忆锚点」通过解析树桩、树杈、树枝、树叶，反向生成上线初始化所用的全库DDL脚本。我们把这一过程叫做反记忆锚点。

## 📦 源码项目结构
```
Stargate/
├── lib/                            # 依赖Jar包（各个数据的JDBC驱动等）
├── resources/                      # 配置文件与执行脚本
│   ├── System.conf                 # 星际之门配置
│   ├── GaussDB.conf                # 「举例子」GaussDB数据库配置
│   ├── GBaseBAMPP.conf             # 「举例子」GBase8AMPP数据库配置
│   ├── GBaseBAMPPparameter.conf    # 「举例子」GBase8AMPP数据库参数配置
│   ├── XXXX.conf                   # XXXX数据库配置
│   ├── XXXXparameter.conf          # XXXX数据库参数配置
│   ├── function.conf               # 封装的一些shell命令
│   ├── Source2Target.sh            # 从「源」到「目标」正向开启量子跃迁桥(多量子)
│   ├── Source2TargetSingle.sh      # 从「源」到「目标」正向开启量子跃迁桥(单量子)
│   ├── Target2Source.sh            # 从「目标」到「源」反向开启量子跃迁桥(多量子)
│   ├── Target2SourceSingle.sh      # 从「目标」到「源」反向开启量子跃迁桥(单量子)
│   ├── QuantumTransitionBrideg.sh  # 量子跃迁桥启动脚本
│   ├── MemoryAnchorStake.sh        # 记忆锚点启动脚本
│   └── AntiMemoryAnchorStake.sh    # 反记忆锚点启动脚本
├── src/                            # 源码目录
│   ├── ink.qicq/
│   │   ├── task/                       # 任务模块
│   │   ├── thread/                     # 线程模块
│   │   └── utils/                      # 工具类
│   ├── QuantumTransitionBrideg.java    # 量子跃迁程序入口
│   ├── MemoryAnchorStake.java          # 记忆锚点程序入口
│   └── XXX.META-INF/
│           └── MANIFEST.MF
├── LICENSE
└── README.md
```
## 🚀 星际之门(部署结构)
```
Stargate/
├── bin/                                # 存放shell脚本
│   ├── Source2Target.sh                # 从「源」到「目标」正向开启量子跃迁桥(多量子)
│   ├── Source2TargetSingle.sh          # 从「源」到「目标」正向开启量子跃迁桥(单量子)
│   ├── Target2Source.sh                # 从「目标」到「源」反向开启量子跃迁桥(多量子)
│   ├── Target2SourceSingle.sh          # 从「目标」到「源」反向开启量子跃迁桥(单量子)
│   ├── QuantumTransitionBrideg.sh      # 量子跃迁桥启动脚本
│   ├── MemoryAnchorStake.sh            # 记忆锚点启动脚本
│   └── AntiMemoryAnchorStake.sh        # 反记忆锚点启动脚本
├── backups/                            # 记忆锚点存放路径
│   ├── XXXX1/                          # 自动生成，只举例子用。
│   │   ├── yyyyMMddHHmmss/             # 自动生成，只举例子用。
│   │   │   ├── schema1/                # 自动生成，只举例子用。
│   │   │   │   ├── xxx/xx              # 自动生成，只举例子用。
│   │   │   │   └── xxx/xx              # 自动生成，只举例子用。
│   │   │   └── schema2/                # 自动生成，只举例子用。
│   │   │       ├── xxx/xx              # 自动生成，只举例子用。
│   │   │       └── xxx/xx              # 自动生成，只举例子用。
│   │   └── yyyyMMddHHmmss/             # 自动生成，只举例子用。
│   ├── XXXX2/                          # 自动生成，只举例子用。
│   └── XXXX1_MASSqlFile/               # 自动生成，只举例子用。
│       ├── Schema1.sql                 # 自动生成，只举例子用。
│       └── Schema2.sql                 # 自动生成，只举例子用。
├── table/                              # 量子跃迁配置。
│   ├── XXXX1/                          # 参考Demo.conf进行修改，最后把.conf去掉。
│   └── XXXX2/                          # 参考Demo.conf进行修改，最后把.conf去掉。
├── logs/                               # 量子跃迁日志文件（记忆锚点日志在每一个记忆锚点中）。
├── lib/                                # 存放量子跃迁桥主程序
│   ├── function.conf                   # 封装的一些shell命令
│   ├── quantum-transition-bridge.jar   # 量子跃迁主程序
│   └── memory-anchor-stake.jar         # 记忆锚点主程序
└── config/
    ├── System.conf                     # 星际之门配置文件
    ├── XXXX.conf                       # 数据库配置文件
    └── XXXXparameter.conf              # 数据库session参数配置文件
```


## 🚀 快速开始
### 如何开启量子跃迁
```bash
sh /xxx/xxx/Stargate/bin/xxx2xxx.sh 参数1 参数2 参数3
sh /xxx/xxx/Stargate/bin/xxx2xxxSingle.sh Schema.TableName DeleteType YYYYMMDD evaID 

#参数1说明
传你想要量子传输的配置文件名字
```
### 如何使用记忆锚点
```bash
#编辑定时任务
crontab -e

#crontab表达式不再过多叙述
xxxxxxxx sh /xxx/xxx/Stargate/bin/MemoryAnchorStake.sh 参数
#参数说明
传你想要生成记忆锚点的数据库名字

#反记忆锚点
sh /xxx/xxx/Stargate/bin/AntiMemoryAnchorStake.sh 参数
#参数说明
传你想要生成记忆锚点的数据库名字
```
## ⚙️ 配置文件说明

### 1. XXXX.conf：数据库参数配置文件。
### 2. XXXXparameter.conf：数据库session参数配置文件。
### 3. System.conf：星际之门配置文件
### 4. Demo.conf：量子跃迁多量子配置文件。
```ini
#这里配置要同步的表。也是未来放在table文件夹下的东东。

#ID序号       表全名		    迁移方向(1则表示从源到目标，2则表示从目标到源头)	删除方式（1Delete全表，2Truncate全表，0不删只插入，3智能建删分区）
1            schema.tables  1						                    0

#删除方式中012随便配，但是3的话，有要求。
分区只支持list分区，并且针对某公司特殊的产品格式，分区格式为：
分区格式为：p_19700101。分区键为：1970-01-01
分区格式为：p_19700101_0。分区键为：19700101_0
所以还是建议自行先创建或清空分区，然后选择0的配置方式进行，选3只针对特殊人群。
```
### 3. 使用注意事项

1. 数据库字符集：现在信创大环境下默认数据库字符集都是UTF8，但也有极个别老旧项目会是GBK，如果遇到数据库字符集不匹配导致中文乱码，可尝试在jdbcurl后面追加字符编码进行尝试。
2. Shell：支持常规的linux系统，因作者很少使用其他不同版本的linux或unix，可能shell脚本在各别系统中无法使用。可自行修改兼容。如果你的开发环境是windows环境，也可以自行编写bat脚本，最核心的功能其实是java -jar调用量子跃迁桥主程序，shell或bat存在的意义只是为了读取配置文件内容并被操作系统调用执行。
3. 量子跃迁生成数据文件：暂不支持，未来可能会做用某种分隔符分隔的数据文件，供下数使用。
4. 因项目中含有1.8特性，所以JDK需要1.8+。（本来可以不用的，但是作者比较懒，后面尽可能优化回来，避免JDK要求。）

## 📜 协议声明

本项目基于 **Apache-2.0 license** 发布，协议详情见`LICENSE`文件。


## ❓ 常见问题

### 1. 报错看日志提示进行分析吧哈哈哈哈哈哈哈哈
就这么逆天！因为我也想不到什么常见问题，配置文件配错了算不算？

## 📞 联系方式

若有问题需要帮助，可通过以下方式联系：（有些联系方式过于逆天，可以忽略）
- 邮箱：1245360000@qq.com
- GitHub：https://github.com/XBBDDBB/MemoryAnchorStake
- B站：******
- Steam：******
- 原神：******
- 抖音：******
- 英雄联盟：******
- 三角洲行动：******
- 。。。。。。