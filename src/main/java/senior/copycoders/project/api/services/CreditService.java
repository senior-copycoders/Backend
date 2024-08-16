package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.factories.PaymentDtoFactory;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;
import senior.copycoders.project.store.repositories.CreditRepository;
import senior.copycoders.project.store.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class CreditService {
    CreditRepository creditRepository;
    PaymentRepository paymentRepository;
    PaymentDtoFactory paymentDtoFactory;

    /**
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка
     * @param creditPeriod   срок кредитования (в месяцах, положительное целое число)
     */
    public CreditEntity saveCredit(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, BigDecimal payment, Integer creditPeriod) {
        CreditEntity saveCredit = creditRepository.save(CreditEntity.builder()
                .initialPayment(initialPayment)
                .creditAmount(creditAmount)
                .percentRate(percentRate)
                .creditPeriod(creditPeriod)
                .payment(payment)
                .build());

        return saveCredit;
    }


    /**
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка
     * @param creditPeriod   срок кредитования (в месяцах, положительное целое число)
     */
    public Object[] calculateAndSave(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {
        // Нам важна точность вычислений, поэтому для вычислений будем использовать BigDecimal

        Object[] data = new Object[2];


        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // вычисляем платёж
        BigDecimal payment = calculatePayment(ostatokOfCredit, percentRate, creditPeriod);

        // Сохраним данный кредит в БД
        CreditEntity saveCredit = saveCredit(initialPayment, creditAmount, percentRate, payment, creditPeriod);
        data[0] = saveCredit.getId();


        // Теперь нужно сформировать список всех платежей
        List<PaymentEntity> payments = createListOfPayments(ostatokOfCredit, payment, percentRate, creditPeriod, saveCredit);

        // Сохраняем список платежей в БД
        paymentRepository.saveAll(payments);

        // Теперь нужно сформировать список paymentDto
        List<PaymentDto> paymentDtos = payments.stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList();

        data[1] = paymentDtos;
        return data;
    }


    private List<PaymentEntity> createListOfPayments(BigDecimal creditAmount, BigDecimal payment, BigDecimal percentRate, Integer creditPeriod, CreditEntity saveCredit) {
        // Общая сумма денег, которую мы заплатим по итогу, равняется payment * creditPeriod

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        BigDecimal totalSum = payment.multiply(BigDecimal.valueOf(creditPeriod));
        List<PaymentEntity> payments = new ArrayList<>();

        // Первая дата платёжа, это текущая дата + 1 месяц
        LocalDate date = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= creditPeriod; i++) {

            // Посчитаем какая часть платежа уйдёт на оплату процентов
            BigDecimal currentPercent = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_EVEN);

            // Посчитаем какая часть платежа уйдёт на погашение основного долга
            BigDecimal repaymentCredit = payment.subtract(currentPercent);

            totalSum = totalSum.subtract(payment);

            creditAmount = (creditAmount.add(currentPercent)).subtract(payment);

            payments.add(PaymentEntity.builder()
                    .paymentNumber(i)
                    .paymentDate(date)
                    .paymentAmount(payment)
                    .percent(currentPercent)
                    .repaymentCredit(repaymentCredit)
                    .remainingCredit(totalSum)
                    .credit(saveCredit)
                    .build());

            date = date.plusMonths(1);

        }

        return payments;
    }

    private BigDecimal calculatePayment(BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {
        // Ежемесячный платеж = (Сумма кредита * Процентная ставка / 12) / (1 - (1 + Процентная ставка / 12)^(-Срок кредита в месяцах))

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        // (ostatokOfCredit * percentRate) / 12
        BigDecimal firstPart = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), RoundingMode.HALF_EVEN);

        // percentRate/12
        BigDecimal secondPart = percentRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_EVEN);

        // 1 + percentRate/12
        BigDecimal thirdPart = BigDecimal.ONE.add(secondPart);

        // (1 + percentRate/12) ^ creditPeriod
        BigDecimal fourthPart = thirdPart.pow(creditPeriod);

        // 1/ ((1 + percentRate/12) ^ creditPeriod)
        BigDecimal fivePart = BigDecimal.ONE.divide(fourthPart, 10, RoundingMode.HALF_EVEN);

        // 1 - (1 + percentRate/12) ^ (-creditPeriod)
        BigDecimal sixPart = BigDecimal.ONE.subtract(fivePart);

        return firstPart.divide(sixPart, 2, RoundingMode.HALF_EVEN);
    }


}
