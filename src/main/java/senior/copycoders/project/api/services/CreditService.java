package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithIdCreditDto;
import senior.copycoders.project.api.factories.CreditDtoFactory;
import senior.copycoders.project.api.factories.PaymentDtoFactory;
import senior.copycoders.project.api.factories.PaymentDtoWithIdCreditDtoFactory;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;
import senior.copycoders.project.store.repositories.CreditRepository;
import senior.copycoders.project.store.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
    PaymentDtoWithIdCreditDtoFactory paymentDtoWithIdCreditDtoFactory;
    CreditDtoFactory creditDtoFactory;

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
    public PaymentWithIdCreditDto calculateAndSave(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {

        // TODO сделать валидацию данных


        // Нам важна точность вычислений, поэтому для вычислений будем использовать BigDecimal

        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // вычисляем платёж
        BigDecimal payment = calculatePayment(ostatokOfCredit, percentRate, creditPeriod);

        // Сохраним данный кредит в БД
        CreditEntity saveCredit = saveCredit(initialPayment, creditAmount, percentRate, payment, creditPeriod);

        // Теперь нужно сформировать список всех платежей
        List<PaymentEntity> payments = createListOfPayments(ostatokOfCredit, payment, percentRate, creditPeriod, saveCredit);

        // Сохраняем список платежей в БД
        paymentRepository.saveAll(payments);

        // Теперь нужно сформировать список paymentDto
        List<PaymentDto> paymentDtos = payments.stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList();


        return paymentDtoWithIdCreditDtoFactory.makePaymentWithIdCreditDto(saveCredit.getId(), paymentDtos);
    }


    /**
     * Метод для вычисления списка платежей по кредиту по месяцам
     *
     * @param creditAmount сумма кредита
     * @param payment      платёж
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @param saveCredit   кредит, к которому привязаны платежи
     * @return список платежей
     */
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


    /**
     * Метод для вычисления платежа
     *
     * @param creditAmount сумма кредита
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @return платёж
     */
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


    /**
     * Получение списка всех платежей по id кредита
     *
     * @param creditId id кредита
     * @return список платежей
     */
    public List<PaymentDto> getAllPaymentsByCreditId(Long creditId) {

        // TODO сделать валидацию случая, когда по id нет кредита

        // получаем кредит по creditId
        CreditEntity credit = creditRepository.findById(creditId).get();

        // формируем список платежей
        List<PaymentDto> payments = new ArrayList<>(credit.getPaymentList().stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList());


        // сортируем список платежей, чтобы они шли по порядку
        Collections.sort(payments);

        return payments;

    }


    /**
     * Возвращает список всех платежей
     *
     * @return
     */
    public List<CreditDto> getAllCredit() {

        List<CreditEntity> creditEntities = creditRepository.findAll();

        return creditEntities.stream()
                .map(creditDtoFactory::makeCreditDto)
                .toList();
    }
}
