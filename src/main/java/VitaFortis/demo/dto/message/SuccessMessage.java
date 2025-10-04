package eRHemunerar.demo.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class SuccessMessage<U> {
    private Boolean success;
    private Object message;
    private Object result;
}