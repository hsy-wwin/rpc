package utils.Request;

import utils.Type;

import java.util.Arrays;

/***
 * 汇报任务的请求参数
 */
public class ReportTaskState {

    public String[] FilesName;

    public String TaskName;

    public Type type;

    public String[] getFilesName() {
        return FilesName;
    }

    public void setFilesName(String[] filesName) {
        FilesName = filesName;
    }

    public String getTaskName() {
        return TaskName;
    }

    public void setTaskName(String taskName) {
        TaskName = taskName;
    }

    public ReportTaskState(){

    }

    public ReportTaskState(String[] filesName, String taskName) {
        FilesName = filesName;
        TaskName = taskName;
    }

    @Override
    public String toString() {
        return "ReportTaskState{" +
                "FilesName=" + Arrays.toString(FilesName) +
                ", TaskName='" + TaskName + '\'' +
                '}';
    }
}
