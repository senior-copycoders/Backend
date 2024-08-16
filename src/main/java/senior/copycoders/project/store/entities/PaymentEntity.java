package senior.copycoders.project.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "payment_number")
    Integer paymentNumber;

    @Column(name = "payment_date")
    LocalDate paymentDate;


    @Column(name = "payment_amount")
    BigDecimal paymentAmount;

    @Column(name = "percent")
    BigDecimal percent;

    @Column(name = "repayment_credit")
    BigDecimal repaymentCredit;


    @Column(name = "remaining_credit")
    BigDecimal remainingCredit;

    @ManyToOne
    CreditEntity credit;

}
