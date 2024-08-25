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
import senior.copycoders.project.store.enums.StatusOfPaymentOrCredit;
import senior.copycoders.project.store.enums.TypeOfCredit;
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
     * @param currentCredit  сущность кредит, к которому привязаны платежи
     */
    public List<PaymentDto> calculatePayments(String dateOfFirstPayment, BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, CreditEntity currentCredit, TypeOfCredit typeOfCredit) {
        // валидация данных для платежа
        controllerHelper.validateDataOfCredit(dateOfFirstPayment, initialPayment, creditAmount, percentRate, creditPeriod);


        // Нам важна точность вычислений, поэтому для вычислений будем использовать BigDecimal

        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);

        // список платежей, который мы будем возвращать
        List<PaymentEntity> payments;

        // дата первого платежа
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateOfFirstPayment, formatter);

        // аннуитет
        if (typeOfCredit == TypeOfCredit.ANNUITY) {
            // вычисляем платёж
            BigDecimal payment = calculatePaymentOfAnnuityCredit(ostatokOfCredit, percentRate, creditPeriod);

            // Теперь нужно сформировать список всех платежей
            payments = createListOfAnnuityCredit(date, ostatokOfCredit, payment, percentRate, creditPeriod, currentCredit, false, BigDecimal.valueOf(0));
        }

        // дифференцированный
        else {
            payments = createListOfDifferentiatedCredit(date, ostatokOfCredit, percentRate, creditPeriod, currentCredit, false, BigDecimal.valueOf(0));
        }

        currentCredit.setPaymentList(payments);
        // Теперь нужно сформировать список paymentDto
        return createListOfPaymentDto(payments);

    }


    /**
     * Метод для вычисления списка платежей по дифференцированному кредиту
     *
     * @param creditAmount  сумма кредита
     * @param percentRate   годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod  срок кредитования в месяцах
     * @param currentCredit кредит, к которому привязаны платежи
     * @return список платежей
     */
    private List<PaymentEntity> createListOfDifferentiatedCredit(LocalDate date, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, CreditEntity currentCredit, boolean isNeedCheck, BigDecimal helpTotalSum) {

        List<PaymentEntity> payments = new ArrayList<>();

        // посчитаем общую сумму долга
        BigDecimal totalSum = createTotalSumForDifferentiatedCredit(creditAmount, percentRate, creditPeriod);

        if (isNeedCheck) {
            if (totalSum.compareTo(helpTotalSum) != 0) {
                totalSum = helpTotalSum;
            }
        }

        // каждый месяц долг должен уменьшаться на эту величину
        BigDecimal decrease = creditAmount.divide(BigDecimal.valueOf(creditPeriod), 2, RoundingMode.HALF_EVEN);

        // r = r / 100;
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 38, RoundingMode.HALF_EVEN);

        // r = r / 12;
        percentRate = percentRate.divide(BigDecimal.valueOf(12), 38, RoundingMode.HALF_EVEN);


        for (int i = 1; i <= creditPeriod; i++) {
            PaymentEntity paymentEntity = new PaymentEntity();

            // начисляем проценты
            BigDecimal percent = creditAmount.multiply(percentRate).setScale(4, RoundingMode.HALF_EVEN);

            // платёж = проценты + постоянная часть
            BigDecimal payment = percent.add(decrease).setScale(4, RoundingMode.HALF_EVEN);

            paymentEntity.setPaymentAmount(payment);

            // привязываем платёж к кредиту
            paymentEntity.setCredit(currentCredit);
            // установка даты
            paymentEntity.setPaymentDate(date);
            // установка общей суммы выплат до платежа
            paymentEntity.setBeforePayment(totalSum);
            // погашение процентов
            paymentEntity.setPercent(percent);
            // погашение долга
            paymentEntity.setRepaymentCredit(decrease);

            // уменьшим текущую сумма долга
            creditAmount = creditAmount.subtract(decrease);
            paymentEntity.setCreditAmount(creditAmount);
            // установим дату следующего платежа
            date = date.plusMonths(1);
            // уменьшим общую сумму выплат
            totalSum = totalSum.subtract(payment).setScale(4, RoundingMode.HALF_EVEN);

            // установим общую сумму выплат после платежа
            paymentEntity.setAfterPayment(totalSum);
            // установим статус - не оплачен
            paymentEntity.setStatus(StatusOfPaymentOrCredit.PENDING);

            // установим номер платежа
            paymentEntity.setPaymentNumber(i);

            payments.add(paymentEntity);
        }

        PaymentEntity lastPayment = payments.get(payments.size() - 1);

        // проверим последний платёж, из-за неточности округления может быть проблемы
        if (lastPayment.getAfterPayment().compareTo(BigDecimal.ZERO) != 0) {
            lastPayment.setPaymentAmount(lastPayment.getPaymentAmount().add(lastPayment.getAfterPayment()));
            lastPayment.setRepaymentCredit(lastPayment.getRepaymentCredit().add(lastPayment.getAfterPayment()));
            lastPayment.setAfterPayment(BigDecimal.ZERO);
        }

        return payments;

    }


    /**
     * Метод для расчёта общей суммы выплат для дифференцированного кредита
     *
     * @param creditAmount сумма кредита
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     */
    private BigDecimal createTotalSumForDifferentiatedCredit(BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {
        // Общая сумма выплат = сумма кредита + r/(100*12) * сумма кредита * (n+1) / 2;

        // r = r / 100;
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 11, RoundingMode.HALF_EVEN);

        // r = r / 12;
        percentRate = percentRate.divide(BigDecimal.valueOf(12), 11, RoundingMode.HALF_EVEN);

        // r/(100*12) * сумма кредита * (n+1) / 2
        BigDecimal result = (percentRate.multiply(creditAmount)).multiply(BigDecimal.valueOf(creditPeriod).add(BigDecimal.valueOf(1))).divide(BigDecimal.valueOf(2), 38, RoundingMode.HALF_EVEN);


        // сумма кредита + r/(100*12) * сумма кредита * (n+1) / 2;
        return result.add(creditAmount).setScale(2, RoundingMode.HALF_EVEN);
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
     * Метод для вычисления платежа по аннуитетному кредиту
     *
     * @param creditAmount сумма кредита
     * @param percentRate  годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod срок кредитования в месяцах
     * @return платёж
     */
    private BigDecimal calculatePaymentOfAnnuityCredit(BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod) {
        // Ежемесячный платеж = (Сумма кредита * Процентная ставка / 12) / (1 - (1 + Процентная ставка / 12)^(-Срок кредита в месяцах))

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

        // (ostatokOfCredit * percentRate) / 12
        BigDecimal firstPart = (creditAmount.multiply(percentRate)).divide(BigDecimal.valueOf(12), 38, RoundingMode.HALF_EVEN);

        // percentRate/12
        BigDecimal secondPart = percentRate.divide(BigDecimal.valueOf(12), 38, RoundingMode.HALF_EVEN);

        // 1 + percentRate/12
        BigDecimal thirdPart = BigDecimal.ONE.add(secondPart);

        // (1 + percentRate/12) ^ creditPeriod
        BigDecimal fourthPart = thirdPart.pow(creditPeriod);

        // 1/ ((1 + percentRate/12) ^ creditPeriod)
        BigDecimal fivePart = BigDecimal.ONE.divide(fourthPart, 38, RoundingMode.HALF_EVEN);

        // 1 - (1 + percentRate/12) ^ (-creditPeriod)
        BigDecimal sixPart = BigDecimal.ONE.subtract(fivePart);

        return firstPart.divide(sixPart, 2, RoundingMode.HALF_EVEN);
    }


    /**
     * Метод для вычисления списка платежей по аннуитетному кредиту
     *
     * @param creditAmount  сумма кредита
     * @param payment       платёж
     * @param percentRate   годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod  срок кредитования в месяцах
     * @param currentCredit кредит, к которому привязаны платежи
     * @return список платежей
     */
    private List<PaymentEntity> createListOfAnnuityCredit(LocalDate dateOfFirstPayment, BigDecimal creditAmount, BigDecimal payment, BigDecimal percentRate, Integer creditPeriod, CreditEntity currentCredit, boolean isNeedCheck, BigDecimal helpTotalSum) {
        // Общая сумма денег, которую мы заплатим по итогу, равняется payment * creditPeriod

        // Преобразуем percentRate, разделив его на 100
        percentRate = percentRate.divide(BigDecimal.valueOf(100), 38, RoundingMode.HALF_EVEN);

        percentRate = percentRate.divide(BigDecimal.valueOf(12), 38, RoundingMode.HALF_EVEN);

        BigDecimal totalSum = payment.multiply(BigDecimal.valueOf(creditPeriod)).setScale(2, RoundingMode.HALF_EVEN);

        if (isNeedCheck) {
            if (totalSum.compareTo(helpTotalSum) != 0) {
                totalSum = helpTotalSum;
            }
        }


        List<PaymentEntity> payments = new ArrayList<>();

        for (int i = 1; i <= creditPeriod; i++) {

            // Посчитаем какая часть платежа уйдёт на оплату процентов
            BigDecimal currentPercent = (creditAmount.multiply(percentRate));

            // Посчитаем какая часть платежа уйдёт на погашение основного долга
            BigDecimal repaymentCredit = payment.subtract(currentPercent).setScale(2, RoundingMode.HALF_EVEN);

            // сумма долга до платежа
            BigDecimal beforePayment = new BigDecimal(totalSum.toString());

            // сумма долга после платежи
            totalSum = totalSum.subtract(payment).setScale(2, RoundingMode.HALF_EVEN);


            // акутальная сумма кредита после платежа
            creditAmount = (creditAmount.add(currentPercent)).subtract(payment);

            payments.add(PaymentEntity.builder()
                    .paymentNumber(i)
                    .paymentDate(dateOfFirstPayment)
                    .paymentAmount(payment)
                    .percent(currentPercent.setScale(4, RoundingMode.HALF_EVEN))
                    .repaymentCredit(repaymentCredit)
                    .afterPayment(totalSum)
                    .credit(currentCredit)
                    .status(StatusOfPaymentOrCredit.PENDING)
                    .beforePayment(beforePayment)
                    .creditAmount(creditAmount)
                    .build());

            dateOfFirstPayment = dateOfFirstPayment.plusMonths(1);

        }

        // проведём стабилизацию последнего платежа
        PaymentEntity lastPayment = payments.get(payments.size() - 1);

        // проверим последний платёж, из-за неточности округления может быть проблемы
        if (lastPayment.getAfterPayment().compareTo(BigDecimal.ZERO) != 0) {
            lastPayment.setPaymentAmount(lastPayment.getPaymentAmount().add(lastPayment.getAfterPayment()));
            lastPayment.setRepaymentCredit(lastPayment.getRepaymentCredit().add(lastPayment.getAfterPayment()));
            lastPayment.setAfterPayment(BigDecimal.ZERO);
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

        LocalDate dateOfPayment = controllerHelper.getDateOrThrowException(date);

        // получим список платежей
        List<PaymentEntity> payments = credit.getPaymentList();

        // отсортируем их по порядку
        Collections.sort(payments);

        // платёж от пользователя
        BigDecimal paymentOfUser = BigDecimal.valueOf(currentPayment);

        // валидация платежа по количеству знаков после запятой и положительности
        if (paymentOfUser.scale() > 2) {
            throw new BadRequestException("Payment contains more than two decimal places.");
        }

        if (paymentOfUser.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("The payment must be greater than zero.");
        }


        // процентная ставка
        BigDecimal percentRate = (credit.getPercentRate().divide(BigDecimal.valueOf(100), 38, RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(12), 38, RoundingMode.HALF_UP);

        // теперь нам нужно найти платёж, который соответствует dateOfPayment
        boolean flag = true;
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentDate().equals(dateOfPayment)) {

                PaymentEntity payment = payments.get(i); // текущая сущность платежа

                // для начала посмотрим, внесён ли уже платёж
                if (StatusOfPaymentOrCredit.PAID == payment.getStatus()) {

                    if (i != payments.size() - 1) {
                        if (payments.get(i + 1).getStatus() == StatusOfPaymentOrCredit.PAID) {
                            throw new BadRequestException("The amount has already been deposited during this period");
                        }

                        int count = i + 2;
                        PaymentEntity newPayment = new PaymentEntity();
                        newPayment.setPaymentNumber(count);
                        count++;
                        newPayment.setPaymentDate(dateOfPayment);
                        newPayment.setBeforePayment(payment.getAfterPayment());
                        newPayment.setStatus(StatusOfPaymentOrCredit.PAID); // ставим статус оплачено
                        newPayment.setPaymentAmount(paymentOfUser); // меняем сумму платежа
                        newPayment.setAfterPayment(newPayment.getBeforePayment().subtract(paymentOfUser));


                        // когда платёж больше чем общая сумма выплат
                        if (paymentOfUser.compareTo(newPayment.getBeforePayment()) > 0) {
                            throw new BadRequestException("The payment must not exceed the total amount of the debt.");
                        }

                        newPayment.setCredit(credit);
                        if (paymentOfUser.compareTo(newPayment.getBeforePayment()) == 0) {
                            // платёж равен остатку долга, то есть пользователь погасил кредит на данном этапе
                            // нужно удалить следующие за ним платежи, так как кредит мы уже выплатили


                            // теперь нужно посчитать сумму процентов
                            BigDecimal sumOfPercent = BigDecimal.ZERO;

                            // и сразу же будем удалять лишние платежи
                            for (int j = payments.size() - 1; j > i; j--) {
                                sumOfPercent = sumOfPercent.add(payments.get(j).getPercent());
                                PaymentEntity remove = payments.remove(j);
                                paymentRepository.delete(remove);
                            }

                            newPayment.setPercent(sumOfPercent); // меняем сумму процентов
                            newPayment.setRepaymentCredit(paymentOfUser.subtract(sumOfPercent)); // вычисляем какая сумма пошла на погашение долга
                            payments.add(newPayment);

                            creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                            return AckDto.makeDefault(true);

                        } else {
                            // поменяем данные текущего платежа
                            PaymentEntity nextPayment = payments.get(i + 1);

                            if (paymentOfUser.compareTo(nextPayment.getPercent()) <= 0) {
                                newPayment.setPercent(paymentOfUser);
                                newPayment.setRepaymentCredit(BigDecimal.ZERO);
                            } else {
                                if (paymentOfUser.compareTo(nextPayment.getRepaymentCredit()) <= 0) {
                                    newPayment.setPercent(BigDecimal.ZERO);
                                    newPayment.setRepaymentCredit(paymentOfUser);
                                } else {
                                    newPayment.setPercent(nextPayment.getPercent());
                                    newPayment.setRepaymentCredit(paymentOfUser.subtract(nextPayment.getPercent()));
                                }
                            }

                            // теперь нужно пересчитать все остальные платежи

                            // создаём список новых платежей
                            List<PaymentEntity> newPayments;
                            // узнаём тип кредита
                            TypeOfCredit typeOfCredit = credit.getTypeOfCredit();


                            // здесь вычисляем какой будет сумма долга после нового платежа
                            BigDecimal currentCreditAmount = payment.getCreditAmount();
                            currentCreditAmount = currentCreditAmount.add(currentCreditAmount.multiply(percentRate));
                            currentCreditAmount = currentCreditAmount.subtract(paymentOfUser);

                            newPayment.setCreditAmount(currentCreditAmount);

                            // аннуитет
                            if (typeOfCredit == TypeOfCredit.ANNUITY) {
                                BigDecimal paymentForNewPlan = newPayment.getAfterPayment().divide(BigDecimal.valueOf(payments.size() - i - 1), 38, RoundingMode.HALF_EVEN);

                                newPayments = createListOfAnnuityCredit(payments.get(i).getPaymentDate(), currentCreditAmount, paymentForNewPlan, credit.getPercentRate(), payments.size() - i - 1, credit, true, newPayment.getAfterPayment());
                            }

                            // дифференцированный
                            else {
                                newPayments = createListOfDifferentiatedCredit(payments.get(i).getPaymentDate(), currentCreditAmount, credit.getPercentRate(), payments.size() - i - 1, credit, true, newPayment.getAfterPayment());
                            }

                            int indexOfOldPayments = i + 1;
                            // теперь нужно сохранить все эти платежи
                            for (int j = 0; j < newPayments.size(); j++) {
                                PaymentEntity paymentToChange = payments.get(indexOfOldPayments); // платёж, который нужно поменять
                                indexOfOldPayments++;
                                PaymentEntity paymentChanging = newPayments.get(j); // платёж, у которого есть данные, чтобы изменить платёж по графику (выше)
                                paymentToChange.setBeforePayment(paymentChanging.getBeforePayment());
                                paymentToChange.setAfterPayment(paymentChanging.getAfterPayment());
                                paymentToChange.setPercent(paymentChanging.getPercent());
                                paymentToChange.setPaymentAmount(paymentChanging.getPaymentAmount());
                                paymentToChange.setRepaymentCredit(paymentChanging.getRepaymentCredit());
                                paymentToChange.setPaymentNumber(count);
                                count++;
                            }

                            payments.add(i + 1, newPayment);

                            creditRepository.save(credit);

                            return AckDto.makeDefault(true);
                        }

                    }


                    // платёж на данную дату уже внесён, генерируем исключение
                    throw new BadRequestException("The loan has already been paid");
                }

                // теперь посмотрим на порядок внесения платежей, вдруг ещё не был внесён платёж по предыдущему платежу
                if (i != 0) {
                    PaymentEntity previous = payments.get(i - 1);
                    if (previous.getStatus() == StatusOfPaymentOrCredit.PENDING) {
                        throw new BadRequestException("First, you need to make payments for previous years.");
                    }
                }

                // установка флага, что мы нашли нужную дату
                flag = false;

                // платёж по плану
                BigDecimal paymentForPlan = payment.getPaymentAmount();


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

                        payment.setStatus(StatusOfPaymentOrCredit.PAID); // ставим статус оплачено
                        payment.setAfterPayment(BigDecimal.ZERO); // сумма после платежа равняется нулю, так как мы весь платёж погасили
                        payment.setPaymentAmount(paymentOfUser); // меняем сумма платежа

                        // теперь нужно посчитать сумму процентов
                        BigDecimal sumOfPercent = payment.getPercent();

                        // и сразу же будем удалять лишние платежи
                        for (int j = payments.size() - 1; j > i; j--) {
                            sumOfPercent = sumOfPercent.add(payments.get(j).getPercent());
                            PaymentEntity remove = payments.remove(j);
                            paymentRepository.delete(remove);
                        }

                        payment.setPercent(sumOfPercent); // меняем сумму процентов
                        payment.setRepaymentCredit(paymentOfUser.subtract(sumOfPercent)); // вычисляем какая сумма пошла на погашение долга

                        creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                        return AckDto.makeDefault(true);

                    } else {


                        // здесь нужно пересчитать все платежи, начиная с i+1 платежа

                        // но сначала поменяем сущность - текущий платёж
                        BigDecimal diff = paymentOfUser.subtract(paymentForPlan);

                        payment.setStatus(StatusOfPaymentOrCredit.PAID); // статус оплачено
                        payment.setRepaymentCredit(payment.getRepaymentCredit().add(diff)); // мы же внесли больше платёж, значит оплатили больше сумма по остатку долга
                        payment.setPaymentAmount(paymentOfUser); // устанавливаем новый платёж


                        // новый остаток долга
                        BigDecimal ostatokAfterNewPayment = payment.getBeforePayment().subtract(paymentOfUser);
                        payment.setAfterPayment(ostatokAfterNewPayment);

                        // теперь нужно пересчитать все платежи
                        int creditPeriod = payments.size() - i - 1; // срок кредитования, который равен количеству оставшихся платежей


                        // вычисляем долг на текущий момент

                        // здесь вычисляем какой будет сумма долга после нового платежа
                        BigDecimal currentCreditAmount = payment.getCreditAmount();
                        currentCreditAmount = currentCreditAmount.subtract(paymentOfUser.subtract(paymentForPlan));
                        payment.setCreditAmount(currentCreditAmount);


                        // создаём список новых платежей
                        List<PaymentEntity> newPayments;
                        // узнаём тип кредита
                        TypeOfCredit typeOfCredit = credit.getTypeOfCredit();


                        // аннуитет
                        if (typeOfCredit == TypeOfCredit.ANNUITY) {
                            BigDecimal paymentForNewPlan = ostatokAfterNewPayment.divide(BigDecimal.valueOf(creditPeriod), 38, RoundingMode.HALF_EVEN);

                            newPayments = createListOfAnnuityCredit(payments.get(i + 1).getPaymentDate(), currentCreditAmount, paymentForNewPlan, credit.getPercentRate(), creditPeriod, credit, true, payments.get(i).getAfterPayment());
                        }

                        // дифференцированный
                        else {

                            newPayments = createListOfDifferentiatedCredit(payments.get(i + 1).getPaymentDate(), currentCreditAmount, credit.getPercentRate(), creditPeriod, credit, true, payments.get(i).getAfterPayment());
                        }

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
                    payment.setStatus(StatusOfPaymentOrCredit.PAID);

                    creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                    return AckDto.makeDefault(true);
                }


            }
        }


        if (flag) {
            // это означает, что в качестве даты платежа была выбрана дата, которой нет в списке платежей
            // значит нужно немного переделать список всех платежей (включая номера платежей)

            // сначала нужно найти между какими датами
            for (int i = 0; i <= payments.size() - 1; i++) {

                // случай, когда платёж раньше официальной даты первого платежа
                if (i == 0) {
                    if (dateOfPayment.isBefore(payments.get(i).getPaymentDate())) {

                        // нужно проверить оплачено ли эта дата (проверка на прошлое)
                        if (payments.get(i).getStatus() == StatusOfPaymentOrCredit.PAID) {
                            throw new BadRequestException("The amount has already been deposited during this period");
                        }


                        PaymentEntity nextPayment = payments.get(0);

                        PaymentEntity newPayment = new PaymentEntity();
                        newPayment.setPaymentNumber(1);
                        newPayment.setPaymentDate(dateOfPayment);
                        newPayment.setStatus(StatusOfPaymentOrCredit.PAID);
                        newPayment.setBeforePayment(nextPayment.getBeforePayment());
                        newPayment.setAfterPayment(nextPayment.getBeforePayment().subtract(paymentOfUser));


                        // когда платёж больше чем общая сумма выплат
                        if (paymentOfUser.compareTo(nextPayment.getBeforePayment()) > 0) {
                            throw new BadRequestException("The payment must not exceed the total amount of the debt.");
                        }

                        newPayment.setPaymentAmount(paymentOfUser);

                        // мы сразу выплатили общую сумму выплат
                        if (paymentOfUser.compareTo(nextPayment.getBeforePayment()) == 0) {


                            BigDecimal sumOfPercent = BigDecimal.ZERO;
                            for (int j = payments.size() - 1; j >= 0; j--) {
                                sumOfPercent = payments.get(j).getPercent();
                                PaymentEntity remove = payments.remove(j);
                                paymentRepository.delete(remove);
                            }

                            newPayment.setPercent(sumOfPercent);
                            newPayment.setCredit(credit);
                            newPayment.setRepaymentCredit(paymentOfUser.subtract(sumOfPercent));

                            payments.add(newPayment);
                            creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                            return AckDto.makeDefault(true);
                        } else {

                            // поменяем данные текущего платежа


                            if (paymentOfUser.compareTo(nextPayment.getPercent()) <= 0) {
                                newPayment.setPercent(paymentOfUser);
                                newPayment.setRepaymentCredit(BigDecimal.ZERO);
                            } else {
                                if (paymentOfUser.compareTo(nextPayment.getRepaymentCredit()) <= 0) {
                                    newPayment.setPercent(BigDecimal.ZERO);
                                    newPayment.setRepaymentCredit(paymentOfUser);
                                } else {
                                    newPayment.setPercent(nextPayment.getPercent());
                                    newPayment.setRepaymentCredit(paymentOfUser.subtract(nextPayment.getPercent()));
                                }
                            }

                            // теперь нужно пересчитать все остальные платежи
                            int count = 2;

                            // создаём список новых платежей
                            List<PaymentEntity> newPayments;
                            // узнаём тип кредита
                            TypeOfCredit typeOfCredit = credit.getTypeOfCredit();


                            // здесь вычисляем какой будет сумма долга после нового платежа
                            BigDecimal currentCreditAmount = credit.getCreditAmount();
                            currentCreditAmount = currentCreditAmount.add(currentCreditAmount.multiply(percentRate));
                            currentCreditAmount = currentCreditAmount.subtract(paymentOfUser);
                            newPayment.setCreditAmount(currentCreditAmount);

                            // аннуитет
                            if (typeOfCredit == TypeOfCredit.ANNUITY) {
                                BigDecimal paymentForNewPlan = newPayment.getAfterPayment().divide(BigDecimal.valueOf(payments.size()), 38, RoundingMode.HALF_EVEN);

                                newPayments = createListOfAnnuityCredit(payments.get(i).getPaymentDate(), currentCreditAmount, paymentForNewPlan, credit.getPercentRate(), payments.size(), credit, true, newPayment.getAfterPayment());
                            }

                            // дифференцированный
                            else {
                                newPayments = createListOfDifferentiatedCredit(payments.get(i).getPaymentDate(), currentCreditAmount, credit.getPercentRate(), payments.size(), credit, true, newPayment.getAfterPayment());
                            }


                            // теперь нужно сохранить все эти платежи
                            for (int j = 0; j < payments.size(); j++) {
                                PaymentEntity paymentToChange = payments.get(j); // платёж, который нужно поменять
                                PaymentEntity paymentChanging = newPayments.get(j); // платёж, у которого есть данные, чтобы изменить платёж по графику (выше)
                                paymentToChange.setBeforePayment(paymentChanging.getBeforePayment());
                                paymentToChange.setAfterPayment(paymentChanging.getAfterPayment());
                                paymentToChange.setPercent(paymentChanging.getPercent());
                                paymentToChange.setPaymentAmount(paymentChanging.getPaymentAmount());
                                paymentToChange.setRepaymentCredit(paymentChanging.getRepaymentCredit());
                                paymentToChange.setPaymentNumber(count);
                                count++;
                            }

                            payments.add(0, newPayment);

                            creditRepository.save(credit);

                            return AckDto.makeDefault(true);


                        }

                    }
                } else {

                    if (i != payments.size() - 1) {
                        // нужно смотреть, чтобы наша дата была между датами
                        if (dateOfPayment.isAfter(payments.get(i).getPaymentDate()) && dateOfPayment.isBefore(payments.get(i + 1).getPaymentDate())) {

                            if (payments.get(i).getStatus() == StatusOfPaymentOrCredit.PENDING) {
                                throw new BadRequestException("First, you need to make payments for previous years.");
                            }

                            if (payments.get(i + 1).getStatus() == StatusOfPaymentOrCredit.PAID) {
                                throw new BadRequestException("The amount has already been deposited during this period.");
                            }

                            PaymentEntity previousPayment = payments.get(i);
                            PaymentEntity newPayment = new PaymentEntity();
                            int count = i + 2;

                            newPayment.setPaymentDate(dateOfPayment);
                            newPayment.setPaymentAmount(paymentOfUser);
                            newPayment.setBeforePayment(previousPayment.getAfterPayment());
                            newPayment.setPaymentNumber(count);
                            count++;
                            // когда платёж больше чем общая сумма выплат
                            if (paymentOfUser.compareTo(newPayment.getBeforePayment()) > 0) {
                                throw new BadRequestException("The payment must not exceed the total amount of the debt.");
                            }

                            // если сразу погасили платёж
                            if (paymentOfUser.compareTo(newPayment.getBeforePayment()) == 0) {

                                newPayment.setAfterPayment(BigDecimal.ZERO);

                                BigDecimal sumOfPercent = BigDecimal.ZERO;
                                for (int j = payments.size() - 1; j >= i + 1; j--) {
                                    sumOfPercent = payments.get(j).getPercent();
                                    PaymentEntity remove = payments.remove(j);
                                    paymentRepository.delete(remove);
                                }

                                newPayment.setPercent(sumOfPercent);
                                newPayment.setCredit(credit);
                                newPayment.setRepaymentCredit(paymentOfUser.subtract(sumOfPercent));

                                payments.add(newPayment);
                                creditRepository.save(credit); // сохраняем кредит вместе с листом payment

                                return AckDto.makeDefault(true);
                            } else {

                                // поменяем данные текущего платежа
                                newPayment.setAfterPayment(newPayment.getBeforePayment().subtract(paymentOfUser));
                                PaymentEntity nextPayment = payments.get(i + 1);

                                if (paymentOfUser.compareTo(nextPayment.getPercent()) <= 0) {
                                    newPayment.setPercent(paymentOfUser);
                                    newPayment.setRepaymentCredit(BigDecimal.ZERO);
                                } else {
                                    if (paymentOfUser.compareTo(nextPayment.getRepaymentCredit()) <= 0) {
                                        newPayment.setPercent(BigDecimal.ZERO);
                                        newPayment.setRepaymentCredit(paymentOfUser);
                                    } else {
                                        newPayment.setPercent(nextPayment.getPercent());
                                        newPayment.setRepaymentCredit(paymentOfUser.subtract(nextPayment.getPercent()));
                                    }
                                }

                                // теперь нужно пересчитать все остальные платежи

                                // создаём список новых платежей
                                List<PaymentEntity> newPayments;
                                // узнаём тип кредита
                                TypeOfCredit typeOfCredit = credit.getTypeOfCredit();


                                // здесь вычисляем какой будет сумма долга после нового платежа
                                BigDecimal currentCreditAmount = previousPayment.getCreditAmount();
                                currentCreditAmount = currentCreditAmount.add(currentCreditAmount.multiply(percentRate));
                                currentCreditAmount = currentCreditAmount.subtract(paymentOfUser);
                                newPayment.setCreditAmount(currentCreditAmount);

                                // аннуитет
                                if (typeOfCredit == TypeOfCredit.ANNUITY) {
                                    BigDecimal paymentForNewPlan = newPayment.getAfterPayment().divide(BigDecimal.valueOf(payments.size() - i - 1), 38, RoundingMode.HALF_EVEN);

                                    newPayments = createListOfAnnuityCredit(payments.get(i).getPaymentDate(), currentCreditAmount, paymentForNewPlan, credit.getPercentRate(), payments.size() - i - 1, credit, true, newPayment.getAfterPayment());
                                }

                                // дифференцированный
                                else {
                                    newPayments = createListOfDifferentiatedCredit(payments.get(i).getPaymentDate(), currentCreditAmount, credit.getPercentRate(), payments.size() - i - 1, credit, true, newPayment.getAfterPayment());
                                }

                                int indexOfOldPayment = i + 1;
                                // теперь нужно сохранить все эти платежи
                                for (int j = 0; j < newPayments.size(); j++) {
                                    PaymentEntity paymentToChange = payments.get(indexOfOldPayment); // платёж, который нужно поменять
                                    indexOfOldPayment++;
                                    PaymentEntity paymentChanging = newPayments.get(j); // платёж, у которого есть данные, чтобы изменить платёж по графику (выше)
                                    paymentToChange.setBeforePayment(paymentChanging.getBeforePayment());
                                    paymentToChange.setAfterPayment(paymentChanging.getAfterPayment());
                                    paymentToChange.setPercent(paymentChanging.getPercent());
                                    paymentToChange.setPaymentAmount(paymentChanging.getPaymentAmount());
                                    paymentToChange.setRepaymentCredit(paymentChanging.getRepaymentCredit());
                                    paymentToChange.setPaymentNumber(count);
                                    count++;
                                }

                                payments.add(i + 1, newPayment);

                                creditRepository.save(credit);

                                return AckDto.makeDefault(true);

                            }


                        }

                    }

                }
            }


        }

        throw new BadRequestException("The date is beyond the payment period.");

    }


    /**
     * Рассчитать платёж по кредиту (для дифференцированного - первый платёж)
     *
     * @param creditAmount   сумма кредита
     * @param percentRate    годовая процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod   срок кредитования в месяцах
     * @param typeOfCredit   тип кредита (либо аннуитет, либо дифференцированный)
     * @param initialPayment начальный платёж
     */
    public BigDecimal findOutThePayment(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, TypeOfCredit typeOfCredit) {

        // сначала валидация данных кредита
        // "2024-08-24" - заглушка, чтобы прошла валидация по дате
        controllerHelper.validateDataOfCredit("2024-08-24", initialPayment, creditAmount, percentRate, creditPeriod);

        BigDecimal payment;
        // для начала нужно подсчитать остаток кредита после начального взноса
        // остаток кредита = creditAmount - initialPayment
        BigDecimal ostatokOfCredit = creditAmount.subtract(initialPayment);


        if (typeOfCredit == TypeOfCredit.ANNUITY) {
            payment = calculatePaymentOfAnnuityCredit(ostatokOfCredit, percentRate, creditPeriod);
        } else {
            // r = r / 100;
            percentRate = percentRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);

            // r = r / 12;
            percentRate = percentRate.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_EVEN);

            // постоянная часть уменьшения кредита
            BigDecimal decrease = ostatokOfCredit.divide(BigDecimal.valueOf(creditPeriod), 4, RoundingMode.HALF_EVEN);

            // тут прибавляем проценты ещё
            payment = decrease.add(percentRate.multiply(ostatokOfCredit));
        }


        return payment;
    }
}
