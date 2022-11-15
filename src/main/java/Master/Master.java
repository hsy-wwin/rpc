package Master;

import Interface.RpcService;
import RPC.Handler.SocketHandler;
import RPC.RpcManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import utils.*;
import utils.Request.ReportTaskState;
import utils.Request.RequestArgs;
import utils.Response.PushTaskState;
import utils.Response.ResponseData;
import utils.Response.ResponseStatus;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RpcService(Master.class)
public class Master {
    //任务队列
    public static Map<String,Task> taskRecord = new ConcurrentHashMap<String,Task>();
    //任务编号
    public static AtomicInteger taskNumber = new AtomicInteger(0);
    //map文件记录 是否给work分发 这个初始化就记录了
    public static Map<String,Type> mRecord = new ConcurrentHashMap<>();
    //浅浅初始化一下,到时手动切片
    static {
        mRecord.put("abc.txt",Type.NotStarted);
        mRecord.put("efg.txt",Type.NotStarted);
    }
    //reduce文件记录
    public static Map<Integer,Type> rRecord = new ConcurrentHashMap<>();
    //map过程生成的文件记录
    public static Map<Integer,String[]> tempReduceFile = new ConcurrentHashMap<>();
    //已经完成的map 和 reduce的个数
    public static int mFinishedCount = 0;

    public static int rFinishedCount = 0;
    //正在运行的map 和 reduce的个数
    public static volatile Integer mRunningCount = 0;

    public static volatile Integer rRunningCount = 0;
    //map阶段是否完成
    public static boolean mapFinished = false;
    //reduce阶段是否完成
    public static boolean reduceFinished = false;
    //需要的reduce 和 map的个数
    private static int reduceNumber = 1;

    private static int mapNumber = 2;

    private static int CurReduceRecordIndex = 1;

    //debug信息
    public void print(){
        System.out.println("------任务队列------");
        Set<String> tasks = taskRecord.keySet();
        for(String task : tasks){
            System.out.println(taskRecord.get(task));
        }
        System.out.println("------任务队列end------");
        System.out.println("任务编号" + taskNumber);
        System.out.println("------mapRecord------");
        Set<String> ms = mRecord.keySet();
        for(String m : ms){
            System.out.println(m + " " +  mRecord.get(m));
        }
        System.out.println("------reduceRecord------");
        Set<Integer> rs = rRecord.keySet();
        for(Integer r : rs){
            System.out.println(r + " " +  rRecord.get(r));
        }
        System.out.println("map running count " + mRunningCount + " reduce running count " + rRunningCount);
        System.out.println("map产生的中间文件start");
        Set<Integer> tempFileIndex = tempReduceFile.keySet();
        for(Integer index : tempFileIndex){
            System.out.println(tempReduceFile.get(index));
        }
        System.out.println("map产生的中间文件end");
    }
    //全局互斥锁
    public static Lock lock = new ReentrantLock();

    public void DoServer() throws Exception{
        boolean b = doRegister();

        ServerSocket server = new ServerSocket(2595);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        while(true){
            Socket client = server.accept();

            executorService.execute(new SocketHandler(client));
        }
    }

    public void DoNettyServer() {
        //扫描rpc服务
        doRegister();
        //定义一个主线程 接收io事件和事件分发
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //定义一个工作线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossGroup,workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列得到连接个数
            .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保持活动连接状态
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    //添加解码器

                    //业务逻辑
                    ch.pipeline().addLast();

                    //添加编码器
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Master master = new Master();
        master.DoServer();
    }
    //将服务信息注册到rpc缓存中,到时可以定时去拉取?
    public boolean doRegister(){
        try {
            RpcManager.init("Master");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RpcService(Master.class)
    //一个客户端过来 就调用本地一次,本地需要阻塞
    public PushTaskState PushTask(RequestArgs args){
        try {
            lock.lock();
            PushTaskState taskState = new PushTaskState();
            taskState.ReduceNumber = reduceNumber;
            int taskNum = taskNumber.getAndAdd(1);
            if(!mapFinished){
                if(mapNumber == mRunningCount){
                    return null;
                }
                taskState.TaskName = "map" + taskNum;
                taskState.TaskType = Type.Map;
                Set<String> tasks = mRecord.keySet();
                for(String mTask : tasks){
                    if(mRecord.get(mTask) == Type.Finished || mRecord.get(mTask) == Type.Processing){
                        continue;
                    }
                    mRecord.replace(mTask,Type.Processing);
                    taskState.MFileName = new String[]{mTask};

                    //创建一个task信息 放到任务队列
                    Task task = new Task();
                    task.MFilesName = taskState.MFileName;
                    task.type = taskState.TaskType;
                    task.name = taskState.TaskName;
                    task.RFilesName = -1;

                    task.status = Type.Processing;

                    taskRecord.putIfAbsent(task.name,task);
                    mRunningCount++;

                    //超时处理
                    new Thread(new HandlerTimeout(lock,taskState.TaskName,taskRecord,mRecord,rRecord,mRunningCount,rRunningCount)).start();
                    return taskState;
                }
                taskState.TaskType = Type.Sleep;
                return taskState;

            }else{
                if(reduceNumber == rRunningCount){
                    return null;
                }
                taskState.TaskName = "reduce" + taskNum;
                taskState.TaskType = Type.Reduce;

                for(int i=1;i<=CurReduceRecordIndex;i++){
                    if(rRecord.get(i) == Type.Processing || rRecord.get(i) == Type.Finished){
                        continue;
                    }
                    else{
                        rRecord.put(i,Type.Processing);
                        taskState.RFileName = tempReduceFile.get(i);

                        Task task = new Task();
                        task.status = Type.Processing;
                        task.name = taskState.TaskName;
                        task.RFilesName = i;
                        task.MFilesName = null;
                        task.type = taskState.TaskType;

                        taskRecord.putIfAbsent(task.name,task);
                        rRunningCount++;
                        //超时处理
                        new Thread(new HandlerTimeout(lock,taskState.TaskName,taskRecord,mRecord,rRecord,mRunningCount,rRunningCount)).start();
                        return taskState;
                    }
                }
                taskState.TaskType = Type.Sleep;
                return taskState;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            print();
            lock.unlock();
        }
        return null;
    }

    @RpcService(Master.class)
    public ResponseData WorkReport(ReportTaskState args){
        try {
            lock.lock();
            //1.得到任务名字
            String taskName = args.TaskName;
            //2.从任务队列中拿到任务 || 后面可能弄个定时任务去删除这个任务，防止内存堆积 ! ! !
            Task task = taskRecord.get(taskName);
            //3.分析任务状态
            Type status = task.status;
            if(status == Type.TimeOut){
                //方便GC
                taskRecord.remove(taskName);
                return new ResponseData(ResponseStatus.TimeOut);
            }
            switch (args.type){
                case Map:
                    //已完成任务加1
                    mFinishedCount++;
                    if(mFinishedCount == mapNumber){
                        mapFinished = true;
                    }
                    //4.记录文件
                    String[] mapFiles = args.FilesName;

                    if(CurReduceRecordIndex == reduceNumber){
                        System.out.println("enter true");
                        // CopyOnWrite
                        Random random = new Random();
                        int index = Math.max(1,random.nextInt(1000) % (reduceNumber + 1));
                        /*
                            //reduce文件记录
                            public Map<Integer,Type> rRecord = new ConcurrentHashMap<>();
                            //map过程生成的文件记录
                            public Map<Integer,String[]> tempReduceFile = new ConcurrentHashMap<>();
                         */
                        //文件负载均衡算法 后面要加
                        String[] curFiles = tempReduceFile.get(index);
                        int allLen = 0;
                        if(curFiles != null) {
                            allLen = curFiles.length + mapFiles.length;
                        }
                        else
                            allLen = mapFiles.length;
                        String[] newFiles = new String[allLen];
                        int pos = 0;
                        if(curFiles != null) {
                            for (String s : curFiles) {
                                newFiles[pos++] = s;
                            }
                        }
                        for(String s : mapFiles){
                            newFiles[pos++] = s;
                        }
                        tempReduceFile.put(index,newFiles);
                        rRecord.put(index,Type.NotStarted);
                        System.out.println("index = " + index);
                    }
                    //如果文件没填满
                    else{
                        System.out.println("enter no");
                        tempReduceFile.putIfAbsent(CurReduceRecordIndex,mapFiles);
                        rRecord.put(CurReduceRecordIndex,Type.NotStarted);
                        CurReduceRecordIndex++;
                    }
                    //5.将map 任务状态置为完成
                    for(String t : task.getMFilesName()){
                        mRecord.put(t,Type.Finished);
                    }
                    //6.移除任务
                    taskRecord.remove(taskName);
                    break;
                case Reduce:
                    rFinishedCount++;
                    if(rFinishedCount == reduceNumber){
                        reduceFinished = true;
                    }
                    Integer rFilesName = task.RFilesName;
                    rRecord.put(rFilesName,Type.Finished);
                    taskRecord.remove(taskName);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("-----------------reported-----------------------");
            print();
            System.out.println("-----------------reportedEnd-----------------------");
            lock.unlock();
            return new ResponseData(ResponseStatus.Ack);
        }
    }
    /***
     * 超时处理接口
     */
    public class HandlerTimeout implements Runnable{

        public Lock lock;

        public String taskName;

        public Map<String,Task> taskMap;

        public Map<String,Type> mRecord;

        public Map<Integer,Type> rRecord;

        public Integer mRunningCount;

        public Integer rRunningCount;

        public HandlerTimeout(Lock lock,String taskName,Map<String,Task> taskMap,Map<String,Type> mRecord,Map<Integer,Type> rRecord,Integer mRunningCount,Integer rRunningCount){
            this.lock = lock;
            this.taskMap = taskMap;
            this.taskName = taskName;
            this.mRecord = mRecord;
            this.rRecord = rRecord;
            this.mRunningCount = mRunningCount;
            this.rRunningCount = rRunningCount;
        }
        @Override
        public void run() {
            try {
                //到时改一下三遍的RTT
                Thread.sleep(15000);
                lock.lock();
                // 如果任务队列还存在这个任务就超时了，那就判断任务类型
                if(taskMap.containsKey(taskName)){
                    Task task = taskMap.get(taskName);
                    if(task.type == Type.Map){
                        Type type = mRecord.get(taskName);
                        if(type == Type.Processing){
                            mRecord.replace(taskName,Type.NotStarted);
                            mRunningCount--;
                        }
                    }
                    else if(task.type == Type.Reduce){
                        Type type = rRecord.get(task.RFilesName);
                        if(type == Type.Processing){
                            rRecord.replace(task.RFilesName,Type.NotStarted);
                            rRunningCount--;
                        }
                    }
                    task.status = Type.TimeOut;
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }
}
