package page;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$$;

public class StartPage {

    private SelenideElement buyButton = $$(By.cssSelector("button")).first();
    private SelenideElement creditButton = $$(By.cssSelector("button")).last();

    public openPaymentPage paymentPage() {
        buyButton.click();
        return new openPaymentPage();
    }

    public openCreditPage creditPage() {
        creditButton.click();
        return new openCreditPage();
    }

}
