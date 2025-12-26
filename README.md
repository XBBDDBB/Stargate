# Stargate
星际之门工程



# 「量子跃迁桥」QuantumTransitionBridge
#### 由于个人时间及接触的项目有限，「量子跃迁桥」暂只支持：
#### 「GaussDB」高斯集中式数据库
#### 「GBase8AMPP」GBase8A分布式数据库
#### 「DM8」达梦8数据库
#### 其余敬请期待。
## 📋 项目介绍
#### 什么？要让我把前台应用和后台批量分别使用不同的数据库？？？而且还是一个mysql系一个pg系？？？什么？？？我司产品不支持多数据源？？？那我怎么把批量跑出来的结果，丢给前台应用供其查看啊？？？🤷要不你整死我吧
#### 什么？开发环境、集成测试环境、业务测试环境、准生产环境，你整这么老多环境，你迁移数据是想累死我嘛？什么？？？还不给ssh权限和命令行工具？？？那我怎么把开发环境的数据迁移到其他环境啊？？？什么？你的意思，让我在开发环境导出来insert语句，然后，打tar包，使用ftp工具下载下来，然后本地打开dbeaver等工具，手动执行？？？🤷要不你整死我吧
#### 什么？！@#¥%……&*()_+
#### 啊哈！
#### 现在有了「量子跃迁桥」，将你的不同语法体系的数据库的数据，通过「量子跃迁」的形式，又快又准的进行互相迁移，从你到我，从我到你！
#### 「量子跃迁桥」支持多表按照直接追加、先删除再插入、先创建分区再插入等方式，进行数据传输。我们把这一过程叫做开启量子跃迁桥。
## 📦 项目结构
```
quantum-transition-bridge/
├── lib/                            # 依赖Jar包（各个数据的JDBC驱动等）
├── resources/                      # 配置文件与执行脚本
│   ├── GaussDB.conf                # 「举例子」GaussDB数据库配置
│   ├── GBaseBAMPP.conf             # 「举例子」GBase8AMPP数据库配置
│   ├── XXXX.conf                   # XXXX数据库配置
│   ├── function.conf               # 封装的一些shell命令
│   ├── Source2Target.sh            # 从「源」到「目标」正向开启量子跃迁桥
│   ├── Target2Source.sh            # 从「目标」到「源」反向开启量子跃迁桥
│   └── QuantumTransitionBrideg.sh  # 量子跃迁桥启动脚本
├── src/                            # 源码目录
│   ├── ink.qicq/
│   │   ├── thread/                 # 线程模块
│   │   └── utils/                  # 工具类
│   ├── Main.java                   # 程序入口
│   └── META-INF/
│       └── MANIFEST.MF
├── LICENSE
└── README.md
```
=======写到这里了
## 🚀 快速开始
### 1. 编译项目，生成「量子跃迁桥」的jar文件
### 2. 在服务器上创建如下目录结构
```
quantum-transition-bridge/
├── bin/                                # 存放shell脚本，将resources目录下的三个shell脚本放在这里。
│   ├── Source2Target.sh                # 从「源」到「目标」正向开启量子跃迁桥
│   ├── Target2Source.sh                # 从「目标」到「源」反向开启量子跃迁桥
│   └── QuantumTransitionBrideg.sh      # 量子跃迁桥启动脚本
├── table/                              # 量子跃迁配置。
│   ├── XXXX1/                          # 参考Demo.conf进行修改，最后把.conf去掉。
│   └── XXXX2/                          # 参考Demo.conf进行修改，最后把.conf去掉。
├── logs/                               # 存放日志文件。
├── lib/                                # 存放量子跃迁桥主程序
│   ├── function.conf                   # 封装的一些shell命令
│   └── quantum-transition-bridge.jar   # 反记忆锚点启动脚本
└── config/
    ├── System.conf                     # 量子跃迁桥自身配置文件
    └── XXXX.conf                       # 数据库配置文件
```
### 3. 将源码当中的对应文件，参考上述目录结构要求，修改并存放在固定位置
### 4. 如何开启量子跃迁
```bash
sh /xxx/xxx/quantum-transition-bridge/bin/xxx2xxx.sh 参数

#参数说明
传你想要量子传输的配置文件名字
```
## ⚙️ 配置文件说明

### 1. XXXX.conf文件。不同数据库自行替换内容，只讲述模板
```ini
#数据库标识，不要乱改，因为代码中有使用，在System.conf中也有使用。
DB_NAME='GBase8AMPP'
#数据库驱动类
DRIVER_CLASS='com.gbase.jdbc.Driver'
#jJDBC连接串，注意不同数据库的高可用方式
JDBC_URL='jdbc:gbase://ip1:port1/database?rewriteBatchedStatements=true&failoverEnable=true&hostList=ip2,ip3'
#连接数据库的用户名
USER_NAME='user'
#连接数据库的密码
PASS_WORD='pass'
```

### 2. System.conf文件。
```ini
#这里是量子跃迁桥系统的配置文件

#结果运行方式。1直接入库。（2生成数据文件，3既入库又生成数据文件，敬请期待）
OUTPUT_TYPE=1
#生成文件的目录地址。（暂不支持，敬请期待）
OUTPUT_URL=NotSupportNow
#数据同步分页大小，如设置成1234567890即无缓冲区，直接全量读全量写。
BUFFER_SIZE=200000
#多少条提交一次。
COMMIT_COUNT=50000
#线程池大小，量力而行。。
POOL_SIZE=10
#错误以后如何处理。1终止并卡死，2继续丢数但不卡死。
ERROR_HANDING_TYPE=1

#配置OLTP和OLAP数据库的配置文件名字。规范定义为OLTP数据库为来源数据库，OLAP数据库为目标数据库，配置文件中迁移方向：1则表示从源到目标，2则表示从目标到源头。
#OLTP数据库名字
OLTP_DB=GaussDB
#OLAP数据库名字
OLAP_DB=GBase8AMPP

```

### 3. Demo.conf文件。
```ini
#这里配置要同步的表。也是未来放在table文件夹下的东东。

#ID序号		表全名		    迁移方向(1则表示从源到目标，2则表示从目标到源头)	删除方式（1Delete全表，2Truncate全表，0不删只插入，3智能建删分区）
1               schema.tables	    1						0

#删除方式中012随便配，但是3的话，有要求。
分区只支持list分区，并且针对某公司特殊的产品格式，分区格式为：
分区格式为：p_19700101。分区键为：1970-01-01
分区格式为：p_19700101_0。分区键为：19700101_0
所以还是建议自行先创建或清空分区，然后选择0的配置方式进行，选3只针对特殊人群。
```
### 3. 使用注意事项

1. 数据库字符集：现在信创大环境下默认数据库字符集都是UTF8，但也有极个别老旧项目会是GBK，如果遇到两个数据库字符集不匹配导致中文乱码，可尝试在jdbcurl后面追加字符编码进行尝试。
2. Shell：支持常规的linux系统，因作者很少使用其他不同版本的linux或unix，可能shell脚本在各别系统中无法使用。可自行修改兼容。如果你的开发环境是windows环境，也可以自行编写bat脚本，最核心的功能其实是java -jar调用量子跃迁桥主程序，shell或bat存在的意义只是为了读取配置文件内容并被操作系统调用执行。
3. 生成数据文件：暂不支持，未来可能会做用某种分隔符分隔的数据文件，供下数使用。


## 📜 协议声明

本项目基于 **Apache-2.0 license** 发布，协议详情见`LICENSE`文件。


## ❓ 常见问题

### 1. 开日志提示进行分析吧哈哈哈哈哈哈哈哈
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




# 「记忆锚点桩」MemoryAnchorStake
#### 由于个人时间及接触的项目有限，「记忆锚点桩」暂只支持：
#### 「GaussDB」高斯集中式数据库
#### 「GBase8AMPP」GBase8A分布式数据库
#### 其余敬请期待。
## 📋 项目介绍
#### 您在当牛做马时，是否有过，修改存储过程或表结构时，自信的都不知道自己是谁，过了许久爆炸了以后痛苦不已，没有备份，现在十分的后悔！
#### 您在当牛做马时，是否有过，下等马队友动了你最为重要的东西，但是由于自己没有想到有朝一日自己可以比窦娥还冤？现在十分的气愤！！！！
#### 您在当牛做马时，是否有过，什么？运维不给ssh权限？不给命令行工具？无法通过命令行导出全库DDL作为上线脚本，还玩个屁？？？？？？？
#### 您在当牛做马时，是否有过，！@#¥%……&*()_+
#### 啊哈！
#### 现在有了「记忆锚点桩」，将你的「记忆」定时的创建「锚点」，使其钉死在「世界树」上。当你想要寻找「失去」的记忆时，可直接使用「锚点」找回当时的自己！
#### 「记忆锚点桩」支持按照死该马（Schema）、按照Type（表、视图、函数、存储过程）、按照奥不债（Object）名字，分类逐一备份DDL。我们把这一过程叫做生成记忆锚点，在您需要时，可对单一对象进行查看及手动恢复。
#### 支持依赖过往「记忆锚点」通过解析树桩、树杈、树枝、树叶，反向生成上线初始化所用的全库DDL脚本。我们把这一过程叫做反记忆锚点。


## 📦 项目结构

```
memory-anchor-stake/
├── lib/                            # 依赖Jar包（各个数据的JDBC驱动等）
├── resources/                      # 配置文件与执行脚本
│   ├── GaussDB.conf                # 「举例子」GaussDB数据库配置
│   ├── GBaseBAMPP.conf             # 「举例子」GBase8AMPP数据库配置
│   ├── GBaseBAMPPparameter.conf    # 「举例子」GBase8AMPP数据库参数配置
│   ├── XXXX.conf                   # XXXX数据库配置
│   ├── XXXXparameter.conf          # XXXX数据库参数配置
│   ├── MemoryAnchorStake.sh        # 记忆锚点启动脚本
│   └── AntiMemoryAnchorStake.sh    # 反记忆锚点启动脚本
├── src/                            # 源码目录
│   ├── ink.qicq/
│   │   ├── task/                   # 备份任务模块
│   │   ├── thread/                 # 线程模块
│   │   └── utils/                  # 工具类
│   ├── Main.java                   # 程序入口
│   └── META-INF/
│       └── MANIFEST.MF
├── LICENSE
└── README.md
```

## 🚀 快速开始
### 1. 编译项目，生成「记忆锚点桩」的jar文件
### 2. 在服务器上创建如下目录结构
```
memory-anchor-stake/
├── bin/                            # 存放shell脚本，将resources目录下的两个shell脚本放在这里。
│   ├── MemoryAnchorStake.sh        # 记忆锚点启动脚本
│   └── AntiMemoryAnchorStake.sh    # 反记忆锚点启动脚本
├── backups/                        # 记忆锚点存放路径，只创建到这里即可。
│   ├── XXXX1/                      # 自动生成，只举例子用。
│   │   ├── yyyyMMddHHmmss/         # 自动生成，只举例子用。
│   │   │   ├── schema1/            # 自动生成，只举例子用。
│   │   │   │   ├── xxx/            # 自动生成，只举例子用。
│   │   │   │   └── xxx/            # 自动生成，只举例子用。
│   │   │   └── schema2/            # 自动生成，只举例子用。
│   │   │       ├── xxx/            # 自动生成，只举例子用。
│   │   │       └── xxx/            # 自动生成，只举例子用。
│   │   └── yyyyMMddHHmmss/         # 自动生成，只举例子用。
│   └── XXXX2/                      # 自动生成，只举例子用。
├── lib/                            # 存放记忆锚点桩主程序
│   └── memory-anchor-stake.jar     # 反记忆锚点启动脚本
└── config/
    ├── XXXX.conf                   # 数据库配置文件
    └── XXXXparameter.conf          # 数据库参数配置文件
```
### 3. 将源码当中的对应文件，参考上述目录结构要求，修改并存放在固定位置
### 4. Crontab开始定时生成「记忆锚点」吧！
```bash
#编辑定时任务
crontab -e

#crontab表达式不再过多叙述
xxxxxxxx sh /xxx/xxx/memory-anchor-stake/bin/MemoryAnchorStake.sh 参数

#参数说明
传你想要生成记忆锚点的数据库名字
```
## ⚙️ 配置文件说明

### 1. XXXX.conf文件。不同数据库自行替换内容，只讲述模板
```ini
#DBName数据库名字，格式不能修改，因为代码中有使用，此DBName应该与数据库配置文件名字、数据库参数配置文件名字完全一致。后续开启记忆锚点桩所传参数也是这个。
DBName='GBase8AMPP'

#数据库驱动类
DBDriver='com.gbase.jdbc.Driver'

#JDBC连接串，注意不同数据库的高可用方式
DBUrl='jdbc:gbase://ip1:port1/database?rewriteBatchedStatements=true&failoverEnable=true&hostList=ip2,ip3'

#连接数据库的用户名
DBUsername='user'

#连接数据库的密码
DBPassword='pass'

#想要生成记忆锚点的schema名字，用英文逗号分隔
BackupUser='schema1,schema2'

#是否开启备份数据（如数据量过大不建议开启，一是定时备份容易撞车，二是对服务器压力过大）
BackupData='0'

#数据同步分页大小（备份数据时使用，不开启数据备份功能不用管）（如设置成1234567890即为无缓冲区，直接全量读全量写）
BufferSize='50000'
```

### 2. XXXXparameter.conf文件。不同数据库自行替换内容，只讲述模板
```ini
#在创建会话连接时，设置会话参数，以便实现自己的需求。不同参数换行写，无需写分号
set session _t_gcluster_support_cte=1

```

### 3. 使用注意事项

1. JDK版本：由于在写反记忆锚点桩时，作者图快较懒，使用了Lambda表达式，因此要想使用反记忆锚点功能需要jdk1.8+。。。。后续会改进成常规写法，尽可能避免过多要求。
2. Shell：支持常规的linux系统，因作者很少使用其他不同版本的linux或unix，可能shell脚本在各别系统中无法使用。可自行修改兼容。如果你的开发环境是windows环境，也可以自行编写bat脚本，最核心的功能其实是java -jar调用记忆锚点桩主程序，shell或bat存在的意义只是为了读取配置文件内容并被操作系统定时调用执行。
3. 数据备份：不建议开启，会对记忆锚点桩所在服务器以及数据库服务器产生较大压力，开启后会生成一个新目录，里面存的是每个表的全量的insert into xxx values xxx语句文件。


## 📜 协议声明

本项目基于 **Apache-2.0 license** 发布，协议详情见`LICENSE`文件。


## ❓ 常见问题

### 1. 开日志提示进行分析吧哈哈哈哈哈哈哈哈
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