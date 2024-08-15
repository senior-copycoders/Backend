package senior.copycoders.project.store.entities;


import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "credit_amount")
    Double creditAmount;

    @Column(name = "percent_rate")
    Double percentRate;

    @Column(name = "credit_period")
    Integer creditPeriod;


    @Column(name = "payment")
    Double payment;


    @Builder.Default
    @OneToMany
    @JoinColumn(name = "credit_id" , referencedColumnName = "id")
    List<PaymentEntity> paymentList = new ArrayList<>();
}
