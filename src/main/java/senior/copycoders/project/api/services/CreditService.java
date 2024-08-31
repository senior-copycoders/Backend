package senior.copycoders.project.api.services;


import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import senior.copycoders.project.api.controllers.helpers.ControllerHelper;
import senior.copycoders.project.api.dto.CreditDto;
import senior.copycoders.project.api.dto.PaymentDto;
import senior.copycoders.project.api.dto.PaymentWithCreditDto;
import senior.copycoders.project.api.factories.CreditDtoFactory;
import senior.copycoders.project.api.factories.PaymentWithCreditDtoFactory;
import senior.copycoders.project.store.entities.CreditEntity;
import senior.copycoders.project.store.entities.PaymentEntity;
import senior.copycoders.project.store.enums.TypeOfCredit;
import senior.copycoders.project.store.repositories.CreditRepository;

import java.math.BigDecimal;
import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
public class CreditService {
    PaymentService paymentService;
    CreditRepository creditRepository;
    PaymentWithCreditDtoFactory paymentDtoWithCreditDtoFactory;
    CreditDtoFactory creditDtoFactory;
    ControllerHelper controllerHelper;


    /**
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка
     * @param creditPeriod   срок кредитования (в месяцах, положительное целое число)
     */
    public CreditEntity saveCredit(BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, BigDecimal payment, Integer creditPeriod, TypeOfCredit typeOfCredit) {
        return creditRepository.save(CreditEntity.builder()
                .initialPayment(initialPayment)
                .creditAmount(creditAmount)
                .percentRate(percentRate)
                .creditPeriod(creditPeriod)
                .payment(payment)
                .typeOfCredit(typeOfCredit)
                .build());

    }

    public CreditEntity saveCredit(CreditEntity credit) {
        return creditRepository.save(credit);

    }

    /**
     * Метод, который сохраняет кредит, вместе с графиком его платежей
     *
     * @param initialPayment первоначальный взнос
     * @param creditAmount   сумма кредита
     * @param percentRate    процентная ставка (именно в процентах, а не в долях, то есть 10, а не 0.1)
     * @param creditPeriod   срок кредитования в месяцах
     */
    public PaymentWithCreditDto calculateSchedule(String dateOfFirstPayment, BigDecimal initialPayment, BigDecimal creditAmount, BigDecimal percentRate, Integer creditPeriod, Boolean type) {

        // для начала установим тип кредита
        // false - аннуитет, когда true - дифф.
        TypeOfCredit typeOfCredit = type ? TypeOfCredit.DIFFERENTIATED : TypeOfCredit.ANNUITY;


        CreditEntity currentCredit = saveCredit(initialPayment, creditAmount, percentRate, BigDecimal.valueOf(1), creditPeriod, typeOfCredit);

        List<PaymentDto> payments = paymentService.calculatePayments(dateOfFirstPayment, initialPayment, creditAmount, percentRate, creditPeriod, currentCredit, typeOfCredit);

        currentCredit.setPayment(payments.get(0).getPaymentAmount());

        return paymentDtoWithCreditDtoFactory.makePaymentWithIdCreditDto(creditDtoFactory.makeCreditDto(currentCredit), payments);
    }


    /**
     * Возвращает список всех кредит
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

        // получаем кредит
        CreditEntity credit = controllerHelper.getCreditOrThrowException(creditId);

        // получаем список всех платежей
        List<PaymentEntity> payments = credit.getPaymentList();

        // удаляем сначала все платежи (так как платежи привязаны к кредиту)
        paymentService.deletePayments(payments);

        // удаляем кредит
        creditRepository.delete(credit);
    }

}
