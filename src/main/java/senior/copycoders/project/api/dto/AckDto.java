package senior.copycoders.project.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.HQLSelect;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "сущность ответ")
public class AckDto {

    @NonNull
    @Schema(description = "true-операция выполнилось успешно, false-нет")
    Boolean answer;

    public static AckDto makeDefault(Boolean answer) {
        return builder()
                .answer(answer)
                .build();
    }
}