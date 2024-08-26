package senior.copycoders.project.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "начальные данные о кредите")
public class InitialDataOfCreditDto {

    @NonNull
    @Schema(description = "налоговый вычет")
    @JsonProperty("tax_deduction")
    BigDecimal taxDeduction;

    @NonNull
    @Schema(description = "платёж")
    BigDecimal payment;

    @NonNull
    @Schema(description = "диапазон ПСК")
    String range;
}
