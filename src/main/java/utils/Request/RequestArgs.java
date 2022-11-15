package utils.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestArgs implements Serializable {
    private ServiceDescriptor service;
    private Object parameters;
}
