package utils.Response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseData{

    public ResponseStatus status;

    public ResponseData(){

    }

    public ResponseData(ResponseStatus status){
        this.status = status;
    }

}
