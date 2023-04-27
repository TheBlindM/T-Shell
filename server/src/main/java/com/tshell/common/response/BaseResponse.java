package com.tshell.common.response;


import jakarta.ws.rs.core.Response;
import lombok.*;



@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    /**
     * Response status.
     */
    private Integer code;

    /**
     * Response message.
     */
    private String message;

    /**
     * Response development message
     */
    private String devMessage;

    /**
     * Response data
     */
    private T data;

    public BaseResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates an ok result with message and data. (Default status is 200)
     *
     * @param data    result data
     * @param message result message
     * @return ok result with message and data
     */
    @NonNull
    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>(Response.Status.OK.getStatusCode(), message, data);
    }


    @NonNull
    public static <T> BaseResponse<T> err(Response.Status status,String message) {
        return new BaseResponse<>(status.getStatusCode(), message, null);
    }

    /**
     * Creates an ok result with message only. (Default status is 200)
     *
     * @param message result message
     * @return ok result with message only
     */
    @NonNull
    public static <T> BaseResponse<T> ok(String message) {
        return ok(message, null);
    }

    /**
     * Creates an ok result with message only. (Default status is 200)
     *
     * @return ok result with message only
     */
    @NonNull
    public static <T> BaseResponse<T> ok() {
        return ok("success", null);
    }

    /**
     * Creates an ok result with data only. (Default message is OK, status is 200)
     *
     * @param data data to response
     * @param <T>  data type
     * @return base response with data
     */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase(), data);
    }
}

