package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.controllers.helpers.ControllerHelper;
import senior.copycoders.project.api.dto.AckDto;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.exceptions.BadRequestException;
import senior.copycoders.project.api.factories.CreditDtoFactory;
import senior.copycoders.project.api.factories.PaymentDtoFactory;
import senior.copycoders.project.api.factories.PaymentWithCreditDtoFactory;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;
import senior.copycoders.project.store.enums.StatusEnum;
import senior.copycoders.project.store.repositories.CreditRepository;
import senior.copycoders.project.store.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    ControllerHelper controllerHelper;
    PaymentRepository paymentRepository;
    PaymentDtoFactory paymentDtoFactory;
    CreditDtoFactory creditDtoFactory;
    PaymentWithCreditDtoFactory paymentWithCreditDtoFactory;
    CreditRepository creditRepository;


    /**
     * Метод, который рассчитывает и сохраняет все платежи по данным кредита
     *
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod   срок кредитования в месяцах
     * @param saveCredit     сущность кредит, к которому привязаны платежи
     */
    public List<PaymentDto> calculateAndSavePayments(String dateOfFirstPayment, BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, CreditEntity saveCredit) {
        // валидация данных для платежа
        controllerHelper.validateDataOfCredit(dateOfFirstPayment, initialPayment, creditAmount, percentRate, creditPeriod);


        // Нам важна точность вычислений, поэтому для вычислений будем использовать BigDecimal

        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // вычисляем платёж
        BigDecimal payment = calculatePayment(ostatokOfCredit, percentRate, creditPeriod);


        // Теперь нужно сформировать список всех платежей
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateOfFirstPayment, formatter);
        List<PaymentEntity> payments = createListOfPayments(date, ostatokOfCredit, payment, percentRate, creditPeriod, saveCredit);

        // Сохраняем список платежей в БД
        paymentRepository.saveAll(payments);

        // Теперь нужно сформировать список paymentDto
        return createListOfPaymentDto(payments);


    }


    /**
     * Получение списка всех платежей по id кредита
     *
     * @param creditId id кредита
     * @return список платежей
     */
    public PaymentWithCreditDto getAllPaymentsByCreditId(Long creditId) {

        // получаем кредит по creditId
        CreditEntity credit = controllerHelper.getCreditOrThrowException(creditId);

        // формируем список платежей
        List<PaymentDto> payments = createListOfPaymentDto(credit);

        // сортируем список платежей, чтобы они шли по порядку
        Collections.sort(payments);

        return paymentWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(credit), payments);

    }

    public List<PaymentDto> createListOfPaymentDto(CreditEntity credit) {
        return new ArrayList<>(credit.getPaymentList().stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList());
    }

    public List<PaymentDto> createListOfPaymentDto(List<PaymentEntity> payments) {
        return payments.stream()
                .map(paymentDtoFactory::makePaymentDto)
                .toList();
    }

    public void deletePayments(List<PaymentEntity> payments) {
        paymentRepository.deleteAll(payments);
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
     * Метод для вычисления списка платежей по кредиту по месяцам
     *
     * @param creditAmount сумма кредита
     * @param payment      платёж
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @param saveCredit   кредит, к которому привязаны платежи
     * @return список платежей
     */
    private List<PaymentEntity> createListOfPayments(LocalDate dateOfFirstPayment, BigDecimal creditAmount, BigDecimal payment, BigDecimal percentRate, Integer creditPeriod, CreditEntity saveCredit) {
        // Общая сумма денег, которую мы заплатим по итогу, равняется payment * creditPeriod

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        BigDecimal totalSum = payment.multiply(BigDecimal.valueOf(creditPeriod));
        List<PaymentEntity> payments = new ArrayList<>();

        for (int i = 1; i <= creditPeriod; i++) {

            // Посчитаем какая часть платежа уйдёт на оплату процентов
            BigDecimal currentPercent = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_EVEN);

            // Посчитаем какая часть платежа уйдёт на погашение основного долга
            BigDecimal repaymentCredit = payment.subtract(currentPercent);

            // сумма долга до платежа
            BigDecimal beforePayment = new BigDecimal(totalSum.toString());

            // сумма долга после платежи
            totalSum = totalSum.subtract(payment);


            // акутальная сумма кредита после платежа
            creditAmount = (creditAmount.add(currentPercent)).subtract(payment);

            payments.add(PaymentEntity.builder()
                    .paymentNumber(i)
                    .paymentDate(dateOfFirstPayment)
                    .paymentAmount(payment)
                    .percent(currentPercent)
                    .repaymentCredit(repaymentCredit)
                    .afterPayment(totalSum)
                    .credit(saveCredit)
                    .status(StatusEnum.PENDING)
                    .beforePayment(beforePayment)
                    .build());

            dateOfFirstPayment = dateOfFirstPayment.plusMonths(1);

        }

        return payments;
    }


    /**
     * Сделать платёж по кредиту
     *
     * @param creditId       id кредита
     * @param date           дата платежа
     * @param currentPayment сумма платежа
     */
    public AckDto makePayment(Long creditId, String date, Double currentPayment) {

        CreditEntity credit = controllerHelper.getCreditOrThrowException(creditId);

        LocalDate dateOfPayment;

        try {
            // Определяем формат даты
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Пытаемся преобразовать строку в LocalDate
            dateOfPayment = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Incorrect date format");
        }

        // получим список платежей
        List<PaymentEntity> payments = credit.getPaymentList();

        // отсортируем их по порядку
        Collections.sort(payments);

        // теперь нам нужно найти платёж, который соответствует dateOfPayment
        boolean flag = true;
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentDate().equals(dateOfPayment)) {

                PaymentEntity payment = payments.get(i); // текущая сущность платежа

                // для начала посмотрим, внесён ли уже платёж
                if (StatusEnum.PAID == payment.getStatus()) {
                    // платёж на данную дату уже внесён, генерируем исключение
                    throw new BadRequestException("The payment has already been made on this date.");
                }

                // теперь посмотрим на порядок внесения платежей, вдруг ещё не был внесён платёж по предыдущему платежу
                if (i != 0) {
                    PaymentEntity previous = payments.get(i - 1);
                    if (previous.getStatus() == StatusEnum.PENDING) {
                        throw new BadRequestException("First, you need to make payments for previous years.");
                    }
                }


                flag = false;

                // платёж по плану
                BigDecimal paymentForPlan = payment.getPaymentAmount();

                // платёж от пользователя
                BigDecimal paymentOfUser = BigDecimal.valueOf(currentPayment);

                // если платёж от пользователя меньше чем платёж по плану - генерируем исключение
                if (paymentOfUser.compareTo(paymentForPlan) < 0) {
                    throw new BadRequestException("The payment has not been accepted, the payment must be at least the scheduled payment.");
                }


                // самая интересная ситуация возникает когда платёж больше чем по плану - тогда нужно делать перерасчёт платежей
                if (paymentOfUser.compareTo(paymentForPlan) > 0) {


                    // проверяем случай когда платёж превысил остаток долга, генерируем исключение
                    if (paymentOfUser.compareTo(payment.getBeforePayment()) > 0) {
                        // платёж превысил остаток долга
                        throw new BadRequestException("The payment must not exceed the total amount of the debt");
                    }

                    // если мы платёжом покрыли сразу весь долг, то нужно закрыть платежи
                    if (paymentOfUser.compareTo(payment.getBeforePayment()) == 0) {
                        // платёж равен остатку долга, то есть пользователь погасил кредит на данном этапе
                        // нужно удалить следующие за ним платежи, так как кредит мы уже выплатили

                        payment.setStatus(StatusEnum.PAID); // ставим статус оплачено
                        payment.setAfterPayment(BigDecimal.ZERO); // сумма после платежа равняется нулю, так как мы весь платёж погасили
                        payment.setPaymentAmount(paymentOfUser); // меняем сумма платежа

                        // теперь нужно посчитать сумма процентов
                        BigDecimal sumOfPercent = payment.getPercent();

                        // и сразу же будем удалять лишние платежи
                        for (int j = payments.size() - 1; j > i; j--) {
                            sumOfPercent = sumOfPercent.add(payments.get(j).getPercent());
                            payments.remove(j);
                        }

                        payment.setPercent(sumOfPercent); // меняем сумма процентов
                        payment.setRepaymentCredit(paymentOfUser.subtract(sumOfPercent)); // вычисляем какая сумма пошла на погашение долга

                        creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                        return AckDto.makeDefault(true);

                    } else {
                        // здесь нужно пересчитать все платежи, начиная с i+1 платежа

                        // но сначала поменяем сущность - текущий платёж
                        BigDecimal diff = paymentOfUser.subtract(paymentForPlan);

                        payment.setStatus(StatusEnum.PAID); // статус оплачено
                        payment.setRepaymentCredit(payment.getRepaymentCredit().add(diff)); // мы же внесли больше платёж, значит оплатили больше сумма по остатку долга
                        payment.setPaymentAmount(paymentOfUser); // уставнавливаем новый платёж


                        // новый остаток долга
                        BigDecimal ostatokAfterNewPayment = payment.getBeforePayment().subtract(paymentOfUser);
                        payment.setAfterPayment(ostatokAfterNewPayment);

                        // теперь нужно пересчитать все платежи
                        int creditPeriod = payments.size() - i - 1; // срок кредитования, который равен количеству оставшихся платежей


                        // вычисляем долг на текущий момент
                        BigDecimal percentRate = (credit.getPercentRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
                        BigDecimal currentCreditAmount = payment.getPercent().divide(percentRate, 4, RoundingMode.HALF_UP);
                        currentCreditAmount = currentCreditAmount.add(payment.getPercent());
                        currentCreditAmount = currentCreditAmount.subtract(paymentOfUser);


                        BigDecimal paymentForNewPlan = ostatokAfterNewPayment.divide(BigDecimal.valueOf(creditPeriod), 4, RoundingMode.HALF_EVEN);

                        List<PaymentEntity> newPayments = createListOfPayments(payments.get(i + 1).getPaymentDate(), currentCreditAmount, paymentForNewPlan, credit.getPercentRate(), creditPeriod, credit);

                        // теперь нужно сохранить все эти платежи

                        int count = 0; // счётчик для newPayments
                        for (int j = i + 1; j < payments.size(); j++) {
                            PaymentEntity paymentToChange = payments.get(j); // платёж, который нужно поменять
                            PaymentEntity paymentChanging = newPayments.get(count); // платёж, у которого есть данные, чтобы изменить платёж по графику (выше)
                            count++;

                            paymentToChange.setBeforePayment(paymentChanging.getBeforePayment());
                            paymentToChange.setAfterPayment(paymentChanging.getAfterPayment());
                            paymentToChange.setPercent(paymentChanging.getPercent());
                            paymentToChange.setPaymentAmount(paymentChanging.getPaymentAmount());
                            paymentToChange.setRepaymentCredit(paymentChanging.getRepaymentCredit());
                        }

                        // сохраняем кредит, вместе с его списком платежей
                        creditRepository.save(credit);

                        return AckDto.makeDefault(true);
                    }

                } else {
                    // если сумма платежа равняется по плану, то просто поставим статус PAID
                    payment.setStatus(StatusEnum.PAID);

                    creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                    return AckDto.makeDefault(true);
                }


            }
        }

        if (flag) {
            // это означает, что в качестве даты платежа была выбрана дата, которой нет в списке платежей
            throw new BadRequestException("The date you selected was not found in the payment schedule.");
        }


        // до этой строчки код никогда не дойдёт
        return AckDto.makeDefault(true);
    }
}
