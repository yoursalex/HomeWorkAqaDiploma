package page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

public class CreditPage {
    private SelenideElement header = $(By.cssSelector("h3"));

    private void headerIsVisible() {
        header.shouldBe(Condition.visible);
    }
}
