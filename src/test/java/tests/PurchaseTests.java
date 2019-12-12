package tests;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.javafaker.Faker;
import data.Card;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import lombok.val;
import page.StartPage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PurchaseTests {

    private Card cardOne = new Card();
    private Card cardTwo = new Card();
    private Card invalidNumberCard = new Card();
    Faker faker = new Faker(new Locale("en"));

    @BeforeEach
    public void setUp() {
        setCards();
    }

    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @Test
    @DisplayName("Должен подтверждать оплату при валидных данных. Карта 1")
    public void shouldConfirmPaymentWithValidDataCardOne() {
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.notificationOkIsVisible());
    }

    @Test
    @DisplayName("Должен подтверждать оплату при валидных данных. Карта 2")
    public void shouldConfirmPaymentWithValidDataCardTwo() {
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardTwo);
        assertTrue(paymentPage.notificationOkIsVisible());
    }

   /* @Test
    @DisplayName("Не должен подтверждаться если неверный год")
    public void bla() {
        val paymentPage = openStartPage().paymentPage();
        cardOne.setYear(setWrongYear());
        assertTrue(paymentPage.notificationErrorIsVisible());
    }*/


    public StartPage openStartPage() {
        open("http://localhost:8080/");
        val StartPage = new StartPage();
        return StartPage;
    }

    public void setCards() {
        cardOne.setNumber("4444 4444 4444 4441");
        cardTwo.setNumber("4444 4444 4444 4442");
        invalidNumberCard.setNumber("4444 4444 4444 4444");
        cardOne.setMonth("01");
        cardTwo.setMonth("01");
        cardOne.setYear(setYear());
        cardTwo.setYear(setYear());
        cardOne.setOwner(setOwner());
        cardTwo.setOwner(setOwner());
        cardOne.setCvc(randomCvc());
        cardTwo.setCvc(randomCvc());
    }

    public String setYear() {
        LocalDate date = LocalDate.now().plusYears(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        String year = date.format(formatter);
        return year;
    }

    public String setWrongYear() {
        LocalDate date = LocalDate.now().minusYears(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        String year = date.format(formatter);
        return year;
    }

    public String setOwner() {
        String owner = faker.name().fullName();
        return owner;
    }

    public String randomCvc() {
        String[] cvcOptions = {"123", "999", "985", "015", "888", "656", "001", "234", "601", "111"};
        int chooseCvc =(int) (Math.random()*cvcOptions.length);
        String cvc = cvcOptions[chooseCvc];
        return cvc;
    }
}
