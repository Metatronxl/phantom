### TODO

- 分发扫描任务：主节点负责管理和分发ip扫描任务，salve节点负责连接主节点和领取任务
```txt
    初步构想是salve通过netty连接到master，
    master检测到有slave连接后会自动分发任务，
    slave完成后返回响应
    
    一个IP segment 作为一个task由master进行分发
    
    master 使用定时任务扫描是否有连接上的slave机器
    
    salve 扫描完成后返回给master 结果， 数据则直接存贮进数据库
    
    
   任务分发的数据库采用redis，所有IP_segment 放入list消息队列
   
   
   
   
   现在的思路是phantom 分为phantom-master和phantom-salve， phantom-master专门做任务的分发和
   IP打分这类的工作，phantom-master将IP-segment存储进redis的消息队列（一个段/24作为一条数据），有salve连接上了就会分发
   一个segment给它。
   
   salve和master通过netty进行通讯，这样可以动态的增减salve的个数
    
    
    
```

- IP打分机制
- 开放使用端口

- 数据库添加扫描的代理类型