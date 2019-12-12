package page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import data.Card;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selectors.byCssSelector;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PaymentPage {
    private SelenideElement header = $(By.cssSelector("h3"));
    private SelenideElement cardNumberField = $(byText("Номер карты")).parent().$(byCssSelector(".input__control"));
    private SelenideElement monthField = $(byText("Месяц")).parent().$(byCssSelector(".input__control"));
    private SelenideElement yearField = $(byText("Год")).parent().$(byCssSelector(".input__control"));
    private SelenideElement ownerField = $(byText("Владелец")).parent().$(byCssSelector(".input__control"));
    private SelenideElement cvcField = $(byText("CVC/CVV")).parent().$(byCssSelector(".input__control"));
    private SelenideElement continueButton = $(byText("Продолжить")).parent().parent();
    private SelenideElement notificationOK = $(byCssSelector(".notification_status_ok"));
    private SelenideElement notificationError = $(byCssSelector(".notification_status_error"));

    public PaymentPage() {
        header.shouldBe(Condition.visible);
    }

    public void fillData(Card card) {
        cardNumberField.setValue(card.getNumber());
        monthField.setValue(card.getMonth());
        yearField.setValue(card.getYear());
        ownerField.setValue(card.getOwner());
        cvcField.setValue(card.getCvc());
        continueButton.click();
    }

    public boolean notificationOkIsVisible() {
        notificationOK.is(Condition.visible);
        return true;
    }

    public boolean notificationErrorIsVisible() {
        notificationError.is(Condition.visible);
        return true;
    }


}
