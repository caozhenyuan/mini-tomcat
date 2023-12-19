# mini-tomcat
学习地址：https://time.geekbang.org/column/intro/100636401?tab=catalog

day01:Tomcat 的内部构造是怎样的吗，共包含哪几部分？

> 1. **Server**：顶级的Tomcat容器，代表整个Tomcat实例。一个Tomcat实例可以包含多个Service组件。
> 2. **Service**：一个Service代表一组可以接收请求并且返回响应的Connector和一个Engine的集合。即，Service处理用户的请求，并返回响应。
> 3. **Connector**：Connector组件负责处理进入和离开Tomcat的网络请求。它用于与客户端建立连接，处理请求，并将响应发送回客户端。比如，Tomcat包括HTTP/1.1和AJP (Apache JServ Protocol) Connector。
> 4. **Engine**：Service中的请求处理核心，用于处理来自Connector的请求。Engine可以包含一个或多个Host组件。
> 5. **Host**：表示一个虚拟主机，即一个域名。在一个Engine中可以有多个Host，Tomcat能够通过Host区分不同的域名，并为每一个域名提供不同的Web应用服务。
> 6. **Context**：一个Context代表一个Web应用程序。在一个Host中可以部署多个Context。Context的路径相当于Web应用程序的根目录。
> 7. **Wrapper**：每个Servlet在Tomcat中都有一个对应的Wrapper，它代表了Servlet的实例和配置。Wrapper属于Context容器。

day02：我们的这个 HTTP Server 只能返回静态资源，没有动态内容，所以不能叫做应用服务器 Application Server，那么从原理上如何将它变成一个应用服务器呢？

> 根据不同的业务需求，动态生成返回数据。

day03:我们现在是简单地通过 URI 中包含 /servlet/ 来判别是否是一个动态 Servlet，有什么更好的办法呢？

> 经典xml配置和@Servlet注解。

day04:我们现在是在一个无限循环中每接收一个 Socket 连接就临时创建一个 Processor 来处理这个 Socket，处理完毕之后再开始下一个循环，这个 Server 是串行工作模式，怎么提高这个 Server 的并发度？

> 引入池化技术以及 Processor 多线程。

day05：Tomcat 为什么用一个简单的 queue 来实现多线程而不是用 JDK 自带的线程池？

> 1.自定义可以更好地控制，还有后期的优化 
>
> 2.历史原因，可能当时内置线程池功能没那么完善 
>
> 现在，应该也支持使用JDK自带的线程池。
