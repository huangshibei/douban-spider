# 豆瓣and知乎爬虫

豆瓣爬虫是一个基于爬取豆瓣各种信息的项目


## 项目初衷
  因为之前在学习并发编程，拜读了《并发变成实战》以及《并发编程艺术》后决定写个项目来巩固一下学到的东西，所以就有写一个多线程爬虫的项目的想法，当然还有另一个原因我觉得爬取大量别人数据好爽啊（难道我是潜在的偷窥狂？！）
## V0.0.1爬取豆瓣电影
  当前版本可以爬取豆瓣电影，后续计划添加音乐，书籍等爬虫功能，最终目标完成一个通用功能只需要一些简单的配置就可以爬取任何信息，甚至其他网站信息（注意：发现豆瓣反扒方式相当无解，无论你以什么方式去查询什么信息只展示前500，所以只能通过各种标签尽可能的爬取更多的信息）>
### 项目主要功能特点
 * 使用HTTP代理
     <br>项目首先会到ip网站获取大量ip，使用的这些ip创建虚拟代理突破网页单个代理访问限制问题
     <br>编写的时候我特意将爬取代理的功能和其他功能解耦了，所以这个功能可以单独拿出来用哦
 * 多线程，高并发进行,效率更快
 * 可拓展新较好，可以在原有代码基础上拓展其他爬虫项目
     
### 爬取的数据
 * 下面是爬取的电影信息（部分信息需要深度爬虫才能获取，本项目中该功能已实现，但暂时未去爬取）
![电影](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/resources/img/movedata.PNG "豆瓣电影数据示例")
### 开发工具以及框架
 * java开发工具：IntelliJ IDEA
 * 项目管理：Maven
 * 版本管理：GitHub
 * 使用数据库： MYSQL5.7
 * 持久层框架：Mybatis
 * 第三方库
    <br>HttpClient4.5-网络请求
    <br>Jsoup-html标签解析
    <br>c3p0 数据库连接池
### 持久化配置
 * 本项目使用MYSQL数据库来保存数据，运行项目之前请自行安装mysql，并在[jdbc.properties](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/resources/jdbc.properties)中配置自己的数据库信息
 
### Quick Start
Run With [StartClass](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com.yao/douban/StartClass.java)

## V0.0.2 添加序列化代理功能
 为了避免每次启动都要重新爬取代理，添加一个序列化代理的功能，当下次启动时先从本地文件中获取已经序列化的代理
 * 实现技术：
   <br>1.在ProxyPool中添加一个Set属性，每当一个代理测试通过可用后存入到set中，没十分钟进行一次序列化<br>
   2.采用了一个读写锁技术，这里的情景是写操作远多于读操作所以使用sychronized或者ReentrantLock对效率的影响并不是特别大 <br>
### 解耦
   今天爬虫是发现，如何爬取代理和其他爬虫功能同时运行会导致同一时间过多线程在运行，正好之前也完成了代理序列化功能所以计划后面讲爬取代理功能模块分离出来，这样也可以单独拿出来用。所以今天将代理爬取和其他功能解耦
## V0.1.0 添加爬取知乎用户模块
 前面有说过因为豆瓣不论以什么方式只能获取500条记录，所以可爬取的数据还是比较少的，本想爬取豆瓣用户但是需要登陆认证我没有搞定，所以就改知乎了
 
 * 实现功能
   爬取知乎用户
 * 实现方式
  <br>
>1知乎用户关注列表不需要我们必须登录，这就免去了登录这块麻烦的地方<br>
>2.通过fiddler查看获取请求api以及请求头；知乎有一个auth认证这个也可以通过谷歌浏览器来获取，获取方式如下：![获取知乎auth方法](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/resources/img/getauth.PNG "获取知乎auth")<br>
>3.获取auth后将其放到请求头头中
   <br>`HttpGet request = new HttpGet(url);`
   <br>`request.setHeader("authorization","oauth " + ZhiHuConfig.authorization);`<br>
>4.关于代理的获取
     <br>前面有提到过将代理获取功能分离出来的事情，所以这里就没有动态获取代理而是单独启动一个线程去反序列化之前已经获取的代理，具体请看：
     [ProxySerializeTask](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/core/task/ProxySerializeTask.java)
## V0.1.1 过滤已经查询过的用户
  我的爬取逻辑是从某个用户开始爬取，然后再更具关注他的用户列表爬取，这样难免会出现重复爬取用户关注列表的情况，所以新建了一张用于存储用户token的表将已经爬取过的用户token存到表中，每次开始爬取新的用户时先查询是否已经爬过
## V1.0.0转成web项目
 公司项目开始用SpringMVC所以最经想要学习一下这个，正好也想把这爬虫项目部署到最经刚买的服务器上所以就转成了web项目，我没有在原来的项目上修改而是重新创建了一个repostory：[爬虫项目Web版](https://github.com/shanyao19940801/Spider-WebApp)
## 1.0.1 添加知乎用户判断机制
 新建了一个token表，此表只有一个字段用于存放已经爬去的用户token，这样每次开始一个用户爬取是就可以判断该用户是否已经爬过，避免大量的重复爬取，因为数据量上去后重复的真的很多。
### update
添加线程池饱和策略，当等待队列满了以后如果还有新的任务直接抛弃


# V2.0.0重构代码
最近在学习设计模式，所以就想着将之前写的代码重构一遍，提高代码质量，增加其可拓展性，减少代码冗余老的版本代码我放到分支了
<br>
重构代码我会尽量区划一些UML设计图<br>

### 设计模式**六个基本原则**
单一职责原则（Single Responsibility Principle,SRP<br>
开闭原则（Open-Closed Principle,OCP）<br>
里氏替换原则（Liskov Substitution Principle,LSP）<br>
依赖倒置原则（Dependency Inversion Principle,DIP）<br>
接口隔离原则（Interface Segregation Principle,ISP）<br>
迪米特法则（Law of Demeter,LoD）

**我目前水平有限只能按照自己的理解尽量把代码质量重构的更高，其中肯定会有很多不合理甚至错误的地方请谅解**

#### 优化代码一

**减少代码冗余，减低耦合度**<br>
对爬取任务代码进行重构，以前的任务代码中有很多重复代码，例如获取任务这段代码每个下载任务都会写一段同样的代码，这里我将重复的代码、属性抽象出来放到抽象类中，同时使用到了**魔板模式**设计模式

* UML结构图
以其中一个任务类为例，画出结构图
![任务类结构图](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/image/spider01.PNG) 

具体类：[AbstractTask](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/core/task/AbstractTask.java)、[ZhiHuUserListTask](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/zhihu/task/ZhiHuUserListTask.java)

* 重构过程中遇到的问题以及解决方式

  **问题**：在抽象类中打印日志，如何确定是哪个子任务出错以便排查错误？<br>
  **解决方法**：利用泛型将子类出入抽象类，然后通过放射机制获取子类类型，在通过这个类型来生成日志打印类实例<br>
具体实现代码请看[AbstractTask](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/core/task/AbstractTask.java)类中的代码段
    

        //通过反射获取泛型的类类型
    Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    //这里是为了在抽象类中打印日志时可以显示其子类名，这样有利于错误排查
    logger = LoggerFactory.getLogger(entityClass);


#### 优化代码二

项目中遇到不同的页面我们需要写不同的算法去解析页面信息以便获取到我们需要的信息，那么随着项目扩大需要解析的页面肯定也会变得越来越多这个时候难免就会出现冗余的代码，随之而来的就是如何选择对应的解析算法。

这时候就想到了一个设计模式：**策略模式**

* UML

![UML](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/image/spider02.PNG)

* 代码

[HtmlParser](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/core/util/HtmlParser.java)

[MoveParser](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/douban/parsers/move/MoveParser.java)

[MoveDetailInfoParser](https://github.com/shanyao19940801/douban-spider/blob/master/douban-spider/src/main/java/com/yao/spider/douban/parsers/move/MoveDetailInfoParser.java)

### 流程图


## 每天必须有产出，哪怕只是一行代码，半页书
* 《我曾其次鄙视自己的灵魂》<br>
第一次，当它本可进取时，却故作谦卑；<br>
第二次，当它在空虚时，用爱欲来填充；<br>
第三次，在困难和容易之间，它选择了容易；<br>
第四次，它犯了错，却借由别人也会犯错来宽慰自己；<br>
第五次，它自由软弱，却把它认为是生命的坚韧；<br>
第六次，当它鄙夷一张丑恶的嘴脸时，却不知那正是自己面具中的一副；<br>
第七次，它侧身于生活的污泥中，虽不甘心，却又畏首畏尾<br>
