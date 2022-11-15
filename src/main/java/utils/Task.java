package utils;

import java.util.Arrays;

public class Task {
    public String name;

    public Type type;

    public Type status;

    public String[] MFilesName;

    public Integer RFilesName;

    public Task(){
    }

    public Task(String name, Type type, Type status, String[] MFilesName, Integer RFilesName) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.MFilesName = MFilesName;
        this.RFilesName = RFilesName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getStatus() {
        return status;
    }

    public void setStatus(Type status) {
        this.status = status;
    }

    public String[] getMFilesName() {
        return MFilesName;
    }

    public void setMFilesName(String[] MFilesName) {
        this.MFilesName = MFilesName;
    }

    public Integer getRFilesName() {
        return RFilesName;
    }

    public void setRFilesName(Integer RFilesName) {
        this.RFilesName = RFilesName;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", MFilesName=" + Arrays.toString(MFilesName) +
                ", RFilesName=" + RFilesName +
                '}';
    }
}
