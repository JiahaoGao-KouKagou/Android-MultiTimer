
# 总说明

## 运行环境
Android 10 及以上

## 功能介绍
MultiTimer可以实现多个计时器同时工作，计时器可随意添加、修改、删除，每个计时器可以独立设置名称、目标时间、铃声，可独立开始和暂停。
与系统自带闹钟的区别在于：
1. MultiTimer音量是铃声音量
2. MultiTimer在关机时不能运行，不能唤醒CPU
3. MultiTimer使用的是相对时间，而系统闹钟用的是绝对时间。

## 数据结构
使用单例模式，所有计时器都存在一个ArrayList中。UI界面上显示每个计时器的Fragment，每个Fragment中有一个计时器的引用，引用Arraylist中的计时器。

## 数据库
数据库名 multitimer.db
其中有一张表 timer
各字段如下
- tag INTEGER PRIMARY KEY                                   主键，计时器的tag
- name VARCHAR NOT NULL DEFAULT 'name'        计时器的名字
- target BIGINT NOT NULL DEFAULT 0                    计时器的目标时间
- ringtone VARCHAR                                               计时器的铃声（Uri字符串）

每次对计时器进行更改时都会同时对数据库进行更改，在程序重启时会先加载数据库中原有的计时器


# 类（模块）介绍

## dboperation 包

### DBHelper
创建数据表timer，参考自网络。

### TimerDBManager
创建数据库multitimer.db，参考自网络。并在其中实现了对timer表的增删改查。


## timer 包

### TimerClass
计时器后端类，包括了
- 计时器的属性：tag、name、isStarted（是否开启）、passed（经过时间）、target（目标时间）、**Uri** ringtoneUri（铃声的Uri）、**Ringtone** ringtone（铃声）
- 计时器的操作：构造方法、各属性的get和set方法、播放铃声、显示弹框、显示通知、删除自己、求剩余时间

### TimerClassArraylist
单例模式的单例类，所有属性和方法均为静态，包括：一个存放计时器的ArrayList，以及对该Arraylist的增加方法、从中根据tag找到计时器的方法、找到距离响铃时间最短的计时器的方法


## 未单独打包

### Lib
工具类，定义了经常使用的变量和方法，所有属性和方法均为静态。包括
- 通知ChannelID CHANNEL_ID_ALARM
- 数据库管理器 TimerDBManager
- 将时间字符串和秒数相互解析的方法
- 从时间字符串或秒数中提取时、分、秒的方法
- 将long型tag转换为String[]的方法
- 创建通知的方法
- 显示通知的方法

### MainActivity
主界面，在其中完成了：加载数据库、新建计时器、显示计时器的Fragment、打开通知权限并创建通知channel、加入后台白名单、注册广播接收者、获取数据库管理器、开启和关闭Service

### HelpActivity
在UI中显示了App的使用方法，在后台实现了获取ContentProvider提供的数据

### SetTimerDialogActivity
用Activity实现的伪弹框，用来设置计时器的目标时间。由TimerFragment开启，获取其传来的数据，并将设置完的数据传回TimerFragment

### TimerBroadcastReceiver
接收息屏和亮屏广播，并发送通知

### TimerContentProvider
将数据库共享，参考自网络

### TimerFragment
计时器在UI上的控制器，每新建一个计时器就在UI上新建一个TimerFragment，通过tag将计时器和TimerFragmert关联起来。删除计时器则是将其对应的TimerFragment删除。

### TimerService
前台Service，在其中开启一个线程，运行剩余时间最短的计时器，到时间后响铃并发送通知。
该Service在MainActivity失去显示时开启、恢复显示时关闭。


# 技术重点

## 单例模式
将计时器的创建封装到单例类中，避免在多个位置重复创建计时器造成混乱。程序中所有的计时器都是对单例的引用，所有计时器都只有一个实例。

## Service中只运行剩余时间最短的计时器
在有多个计时器同时运行时MainActivity若失去显示，则只找出剩余时间最短的计时器在后台运行。因为即使后台有多个计时器同时运行，用户也会先被剩余时间最短的计时器提醒。当用户打开MainActivity时，各计时器又恢复正常。所以这种设计和多个计时器基本等效，简化了程序设计，也减少了资源消耗。

## ArrayList删除
删除一个计时器时会根据tag将其对应的数据库记录删除、并将其tag对应的Fragment删除，而不会删除单例ArrayList中的计时器。在下次启动程序时，从数据库中读不出该计时器，便等价于从ArrayList中删除了。

## 页面跳转和返回
使用ActivityResultLauncher进行页面跳转，简洁地实现了在指定Fragment中打开设置时间对话框，传入原来的数据，再将修改后的数据传回该Fragment。
详见TimerFragment类的init()函数开头。

## Q&A
Q：计时器和Fragment是如何关联起来的？
A：计时器TimerClass的tag属性、Fragment的tag（内置、隐式的）、数据表的tag列，三者是一样的，以此关联。

Q：tag从何而来？
A：在SQLlite数据库中插入新记录时会返回该记录的自增主键，即tag字段，以此创建TimerClass实例和Fragment。

Q：ArrayList中的index和计时器的tag是一样的吗？
A：不是。对ArrayList的操作已经封装在单例类中了，在使用时直接调用公共方法即可，不会因index和tag的区别产生混乱。

Q：计时器的数量有限制吗？
A：受tag属性int类型限制，最多可以有Integer.MAX_VALUE个计时器。

Q：多个计时器是在同一个线程运行的吗？
A：是的。多个计时器的Fragment都显示在UI线程，依靠chromecer组件实现计时，该组件的listener需要依靠在UI上显示来工作。而当切换到后台时，只有一个计时器会工作，其他的都会停止，详见前文。


