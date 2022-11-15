package utils.Response;

import utils.Type;

import java.util.Arrays;

/***
 * work向master请求任务，返回的任务信息
 */
public class PushTaskState {
    //记录map文件的名字
    public String[] MFileName;
    //记录该worker执行的唯一标识
    public String TaskName;
    //记录reduce文件的名字
    public String[] RFileName;

    public Type TaskType;
    //reduce的worker数量
    public int ReduceNumber;

    public PushTaskState(){

    }

    public String[] getMFileName() {
        return MFileName;
    }

    public void setMFileName(String[] MFileName) {
        this.MFileName = MFileName;
    }

    public String getTaskName() {
        return TaskName;
    }

    public void setTaskName(String taskName) {
        TaskName = taskName;
    }

    public String[] getRFileName() {
        return RFileName;
    }

    public void setRFileName(String[] RFileName) {
        this.RFileName = RFileName;
    }

    public Type getTaskType() {
        return TaskType;
    }

    public void setTaskType(Type taskType) {
        TaskType = taskType;
    }

    public int getReduceNumber() {
        return ReduceNumber;
    }

    public void setReduceNumber(int reduceNumber) {
        ReduceNumber = reduceNumber;
    }

    public PushTaskState(String[] MFileName, String taskName, String[] RFileName, Type taskType, int reduceNumber) {
        this.MFileName = MFileName;
        TaskName = taskName;
        this.RFileName = RFileName;
        TaskType = taskType;
        ReduceNumber = reduceNumber;
    }

    @Override
    public String toString() {
        return "PushTaskState{" +
                "MFileName=" + Arrays.toString(MFileName) +
                ", TaskName='" + TaskName + '\'' +
                ", RFileName=" + Arrays.toString(RFileName) +
                ", TaskType=" + TaskType +
                ", ReduceNumber=" + ReduceNumber +
                '}';
    }
}
