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
public class PaymentWithCreditDto {

    @NonNull
    @JsonProperty("credit")
    @Schema(description = "информация про кредит")
    CreditDto credit;


    @NonNull
    @Schema(description = "список платежей")
    List<PaymentDto> payments;
}
