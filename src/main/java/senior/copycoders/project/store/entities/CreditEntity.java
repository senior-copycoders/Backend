package senior.copycoders.project.store.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "initial_payment")
    BigDecimal initialPayment;

    @Column(name = "credit_amount")
    BigDecimal creditAmount;

    @Column(name = "percent_rate")
    BigDecimal percentRate;

    @Column(name = "credit_period")
    Integer creditPeriod;


    @Column(name = "payment")
    BigDecimal payment;


    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "credit_id" , referencedColumnName = "id")
    List<PaymentEntity> paymentList = new ArrayList<>();
}
