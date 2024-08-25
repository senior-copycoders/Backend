package senior.copycoders.project.store.entities;

import jakarta.persistence.*;
import lombok.*;
import senior.copycoders.project.store.enums.StatusOfPaymentOrCredit;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity implements Comparable<PaymentEntity> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "payment_number")
    Integer paymentNumber;

    @Column(name = "payment_date")
    LocalDate paymentDate;


    @Column(name = "payment_amount")
    BigDecimal paymentAmount;

    @Column(name = "percent", precision = 38, scale = 11)
    BigDecimal percent;

    @Column(name = "repayment_credit")
    BigDecimal repaymentCredit;

    @Column(name = "credit_after-payment")
    BigDecimal afterPayment;

    @ManyToOne
    CreditEntity credit;

    @Column(name = "status")
    StatusOfPaymentOrCredit status;

    @Column(name = "credit_before-payment")
    BigDecimal beforePayment;

    @Column(name = "credit_amount")
    BigDecimal creditAmount;

    @Override
    public int compareTo(PaymentEntity o) {
        return Integer.compare(paymentNumber, o.getPaymentNumber());
    }
}
