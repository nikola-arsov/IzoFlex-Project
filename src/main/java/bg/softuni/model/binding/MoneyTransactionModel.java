package bg.softuni.model.binding;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class MoneyTransactionModel {
    private BigDecimal amount;

    public MoneyTransactionModel() {
        this.amount = BigDecimal.ZERO;
    }
    public MoneyTransactionModel(BigDecimal price) {
       this.setAmount(price);
    }

    @DecimalMin(value = "0.01", message = "Сумата не може да бъде по-малка от 0.01 евро!")
    @DecimalMax(value = "5000000", message = "Сумата не може да надвишава 5 милиона евро!")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
