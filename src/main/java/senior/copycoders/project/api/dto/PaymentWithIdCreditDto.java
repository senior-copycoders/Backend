package senior.copycoders.project.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "список платежей с id кредита, к которому они привязаны")
public class PaymentWithIdCreditDto {

    @NonNull
    @JsonProperty("credit_id")
    @Schema(description = "id кредита")
    Long creditId;


    @NonNull
    @Schema(description = "список платежей")
    List<PaymentDto> payments;
}
