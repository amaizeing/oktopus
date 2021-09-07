package test.response.wrap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage<T> {

    private T data;
    private String message;
    private Map<String, String> metadata;

}
