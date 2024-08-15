package senior.copycoders.project.store.entities;

import jakarta.persistence.*;
import lombok.*;

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
    LocalDateTime paymentDate;


    @Column(name = "payment_amount")
    Double paymentAmount;

    @Column(name = "percent")
    Double percent;

    @Column(name = "repayment_credit")
    Double repaymentCredit;


    @Column(name = "remaining_credit")
    Double remainingCredit;

    @ManyToOne
    CreditEntity credit;

}
