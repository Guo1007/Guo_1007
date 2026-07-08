package gcy.system.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private Boolean success;

    private String msg;

    private Object data;

    private Long total;

    private Integer code;

    public static Result ok() {
        return new Result(true, null, null, null, 200);
    }

    public static Result ok(Object data) {
        return new Result(true, null, data, null, 200);
    }

    public static Result okMsg(String msg) {
        return new Result(true, msg, null, null, 200);
    }

    public static Result okMsg(String msg, Object data) {
        return new Result(true, msg, data, null, 200);
    }

    public static Result ok(List<?> data, Long total) {
        return new Result(true, null, data, total, 200);
    }

    public static Result fail(String msg) {
        return new Result(false, msg, null, null, 500);
    }

    public static Result fail(Integer code, String msg) {
        return new Result(false, msg, null, null, code);
    }
}
