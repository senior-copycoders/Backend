package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.factories.CreditDtoFactory;
import senior.copycoders.project.api.factories.PaymentDtoFactory;
import senior.copycoders.project.api.factories.PaymentWithCreditDtoFactory;
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
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class CreditService {
    CreditRepository creditRepository;
    PaymentRepository paymentRepository;
    PaymentDtoFactory paymentDtoFactory;
    PaymentWithCreditDtoFactory paymentDtoWithCreditDtoFactory;
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
    public PaymentWithCreditDto calculateAndSave(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {

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


        return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(saveCredit), paymentDtos);
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
    public PaymentWithCreditDto getAllPaymentsByCreditId(Long creditId) {

        // TODO сделать валидацию случая, когда по id нет кредита

        // получаем кредит по creditId
        CreditEntity credit = creditRepository.findById(creditId).get();

        // формируем список платежей
        List<PaymentDto> payments = new ArrayList<>(credit.getPaymentList().stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList());


        // сортируем список платежей, чтобы они шли по порядку
        Collections.sort(payments);

        return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), payments);

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


    /**
     * Удаляет кредит по id вместе с его платежа
     *
     * @param creditId id кредита
     */
    public void deleteCreditAndPayments(Long creditId) {

        // TODO сделать валидацию

        // получаем кредит
        CreditEntity credit = creditRepository.findById(creditId).get();

        // получаем список всех платежей
        List<PaymentEntity> payments = credit.getPaymentList();

        // удаляем сначала все платежи (так как платежи привязаны к кредиту)
        paymentRepository.deleteAll(payments);

        // удаляем кредит
        creditRepository.delete(credit);
    }


    /**
     * Метод,который меняет данные кредита и делает перерасчёт платежей
     *
     * @param creditId               id кредита
     * @param optionalInitialPayment начальный платёж
     * @param optionalCreditAmount   сумма кредита
     * @param optionalPercentRate    годовая процентная ставка
     * @param optionalCreditPeriod   срок кредитования в месяцах
     */
    public PaymentWithCreditDto changeCreditAndPayments(Long creditId, Optional<BigDecimal> optionalInitialPayment, Optional<BigDecimal> optionalCreditAmount, Optional<BigDecimal> optionalPercentRate, Optional<Integer> optionalCreditPeriod) {

        // TODO сделать валидацию данных

        // сначала получим кредит по id
        CreditEntity credit = creditRepository.findById(creditId).get();

        List<PaymentEntity> paymentsOld = credit.getPaymentList();

        // сначала проверим, вдруг все параметры пустые -> вернём уже имеющийся список платежей и кредит
        if (optionalInitialPayment.isEmpty() && optionalCreditAmount.isEmpty() && optionalPercentRate.isEmpty() && optionalCreditPeriod.isEmpty()) {
            return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), paymentsOld.stream().map(paymentDtoFactory::makePaymentDto).toList());
        }

        // если все параметры равны уже имеющимся
        boolean flag = equalityCheck(credit, optionalInitialPayment, optionalCreditAmount, optionalPercentRate, optionalCreditPeriod);

        if (flag) {
            // значит параметры были поменяны на такие же, вернём просто список платежей и кредит, который у нас есть
            return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), paymentsOld.stream().map(paymentDtoFactory::makePaymentDto).toList());
        }

        // TODO валидация данных о кредите

        // теперь нужно поменять информацию о кредите, и пересчитать платежи
        BigDecimal initialPayment = optionalInitialPayment.orElse(credit.getInitialPayment());
        BigDecimal creditAmount = optionalCreditAmount.orElse(credit.getCreditAmount());
        BigDecimal percentRate = optionalPercentRate.orElse(credit.getPercentRate());
        Integer creditPeriod = optionalCreditPeriod.orElse(credit.getCreditPeriod());

        // расчитаем остаток по кредиту
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // рассчитаем платёж
        BigDecimal payment = calculatePayment(ostatokOfCredit, percentRate, creditPeriod);

        // рассчитаем платежи
        List<PaymentEntity> newPayments = createListOfPayments(ostatokOfCredit, payment, percentRate, creditPeriod, credit);

        // удалим из БД старые платежи
        paymentRepository.deleteAll(paymentsOld);

        // теперь поменяем информацию о кредите в БД
        credit.setCreditAmount(creditAmount);
        credit.setPayment(payment);
        credit.setInitialPayment(initialPayment);
        credit.setPercentRate(percentRate);
        credit.setCreditPeriod(creditPeriod);
        credit.setPaymentList(newPayments);

        // теперь сохраним новые платежи
        paymentRepository.saveAll(newPayments);


        // сохраняем измнения сущности
        credit = creditRepository.save(credit);

        return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), newPayments.stream().map(paymentDtoFactory::makePaymentDto).toList());
    }


    /**
     * Проверяет, что данные credit совпадают со всеми переданными
     *
     * @param credit                 сущность кредит
     * @param optionalInitialPayment начальный платёж
     * @param optionalCreditAmount   сумма кредита
     * @param optionalPercentRate    процентная ставка
     * @param optionalCreditPeriod   срок кредитования в месяцах
     */
    private boolean equalityCheck(CreditEntity credit, Optional<BigDecimal> optionalInitialPayment, Optional<BigDecimal> optionalCreditAmount, Optional<BigDecimal> optionalPercentRate, Optional<Integer> optionalCreditPeriod) {

        if (optionalInitialPayment.isPresent()) {
            BigDecimal initialPayment = optionalInitialPayment.get();
            if (!credit.getInitialPayment().equals(initialPayment)) {
                return false;
            }
        }

        if (optionalCreditAmount.isPresent()) {
            BigDecimal creditAmount = optionalCreditAmount.get();
            if (!credit.getCreditAmount().equals(creditAmount)) {
                return false;
            }
        }

        if (optionalPercentRate.isPresent()) {
            BigDecimal percentRate = optionalPercentRate.get();
            if (!credit.getPercentRate().equals(percentRate)) {
                return false;
            }
        }

        if (optionalCreditPeriod.isPresent()) {
            Integer creditPeriod = optionalCreditPeriod.get();
            return credit.getCreditPeriod().equals(creditPeriod);
        }

        return true;

    }
}
