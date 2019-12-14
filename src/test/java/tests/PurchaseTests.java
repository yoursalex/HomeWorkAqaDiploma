package tests;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.javafaker.Faker;
import data.Card;
import data.SQLHelper;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import page.CreditPage;
import page.PaymentPage;
import page.StartPage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PurchaseTests {

    private Card cardOne = new Card();
    private Card cardTwo = new Card();
    private Card invalidNumberCard = new Card();
    Faker faker = new Faker(new Locale("en"));

    @BeforeEach
    void setUp() {
        setCards();
    }

    @AfterEach
    void cleanTables() throws SQLException {
        SQLHelper.cleanTables();
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
    @DisplayName("Должен подтверждать оплату при валидных данных и карте со статусом APPROVED")
    void shouldConfirmPaymentWithValidDataCardOne() throws SQLException {
        assertTrue(paymentPage(cardOne).notificationOkIsVisible());
        assertEquals(SQLHelper.findStatus(), "APPROVED");
    }

    @Test
    @DisplayName("Должен подтверждать кредит при валидных данных и карте со статусом APPROVED")
    void shouldConfirmCreditWithValidDataCardOne() {
        assertTrue(creditPage(cardOne).notificationOkIsVisible());
        // дополнить проверкой БД
    }

    @Test
    @DisplayName("Не должен подтверждать оплату при использовании карты со статусом DECLINED")
    void shouldNotConfirmPaymentWithInvalidCardTwo() throws SQLException{
        assertTrue(paymentPage(cardTwo).notificationErrorIsVisible());
        // дополнить проверкой БД
    }

    @Test
    @DisplayName("Не должен подтверждать кредит при использовании карты со статусом DECLINED")
    void shouldNotConfirmCreditWithInvalidCardTwo() throws SQLException {
        assertTrue(creditPage(cardTwo).notificationErrorIsVisible());
        // дополнить проверкой БД
    }

    // Негативные сценарии с номером карты при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCard.cvs", numLinesToSkip = 1)
    void shouldNotSubmitPaymentWithWrongNumber(String number, String message) {
        cardOne.setNumber(number);
        assertTrue(paymentPage(cardOne).inputInvalidFormat(), message);
    }

    @Test
    @DisplayName("Не должен подтверждать оплату при невалидном номере карты")
    void shouldNotSubmitPaymentWithIllegalCard() {
        cardOne.setNumber("4444 4444 4444 4444");
        assertTrue(paymentPage(cardOne).notificationErrorIsVisible());
    }

    @Test
    @DisplayName("Оплата.После ввода валидного номера карты, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidCardNumberUpdatedForPayment() {
        cardOne.setNumber("4444 4444 44");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage(cardOne).inputInvalidFormat());
        cardOne.setNumber("4444 4444 4444 4441");
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с номером карты при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCard.cvs", numLinesToSkip = 1)
    void shouldNotSubmitCreditWithWrongNumber(String number, String message) {
        cardOne.setNumber(number);
        assertTrue(creditPage(cardOne).inputInvalidFormat(), message);
    }

    @Test
    @DisplayName("Не должен подтверждать кредит при невалидном номере карты")
    void shouldNotSubmitCreditWithIllegalCard() {
        cardOne.setNumber("4444 4444 4444 4444");
        assertTrue(creditPage(cardOne).notificationErrorIsVisible());
    }

    @Test
    @DisplayName("Кредит.После ввода валидного номера карты, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidCardNumberUpdatedForCredit() {
        cardOne.setNumber("4444 4444 44");
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFormat());
        cardOne.setNumber("4444 4444 4444 4441");
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с датой при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongMonth.cvs", numLinesToSkip = 1)
    void shouldNotSubmitPaymentWithWrongMonth(String month, String message) {
        cardOne.setMonth(month);
        assertTrue((paymentPage(cardOne).inputInvalidFormat()), message);
    }

    @Test
    @DisplayName("Не должен подтверждать оплату, если введен несуществующий месяц")
    void shouldNotConfirmPaymentWithInvalidMonth() {
        cardOne.setMonth("22");
        assertTrue(paymentPage(cardOne).inputInvalidMonth());
    }

    @Test
    @DisplayName("Не должен подтверждать оплату без указания года")
    void shouldNotConfirmPaymentIfEmptyYear() {
        cardOne.setYear("");
        assertTrue(paymentPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Не должен подтверждать оплату, если год меньше текущего")
    void shouldNotConfirmPaymentWithOldYear() {
        cardOne.setYear(setWrongYear());
        assertTrue(paymentPage(cardOne).inputInvalidExpireDate());
    }

    @Test
    @DisplayName("Оплата. После ввода валидной даты предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidDateUpdatedForPayment() {
        cardOne.setMonth("");
        cardOne.setYear("");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidFormat());
        cardOne.setMonth("01");
        cardOne.setYear(setCorrectYear());
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с датой при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongMonth.cvs", numLinesToSkip = 1)
    void shouldNotSubmitCreditWithWrongMonth(String month, String message) {
        cardOne.setMonth(month);
        assertTrue((creditPage(cardOne).inputInvalidFormat()), message);
    }

    @Test
    @DisplayName("Не должен подтверждать оплату, если введен несуществующий месяц")
    void shouldNotConfirmCreditWithInvalidMonth() {
        cardOne.setMonth("22");
        assertTrue(creditPage(cardOne).inputInvalidMonth());
    }

    @Test
    @DisplayName("Не должен подтверждать оплату без указания года")
    void shouldNotConfirmCreditIfEmptyYear() {
        cardOne.setYear("");
        assertTrue(creditPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Не должен подтверждать оплату, если год меньше текущего")
    void shouldNotConfirmCreditWithOldYear() {
        cardOne.setYear(setWrongYear());
        assertTrue(creditPage(cardOne).inputInvalidExpireDate());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной даты предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidDateUpdatedForCredit() {
        cardOne.setMonth("");
        cardOne.setYear("");
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFormat());
        cardOne.setMonth("01");
        cardOne.setYear(setCorrectYear());
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с полем владелец при покупке:

    @Test
    @DisplayName("Не должен подтверждать оплату без имени владельца")
    void shouldNotConfirmPaymentWithoutOwner() {
        cardOne.setOwner("");
        assertTrue(paymentPage(cardOne).inputInvalidFillData());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongOwner.cvs", numLinesToSkip = 1)
    void shouldNotConfirmPaymentWithInvalidOwner(String owner) {
        cardOne.setOwner(owner);
        assertTrue(paymentPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Оплата. После ввода валидной информации в поле владелец, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidOwnerUpdatedForPayment() {
        cardOne.setOwner("");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidFillData());
        cardOne.setOwner(setFakeOwner());
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с полем владелец при кредите:

    @Test
    @DisplayName("Не должен подтверждать кредит без имени владельца")
    void shouldNotConfirmCreditWithoutOwner() {
        cardOne.setOwner("");
        assertTrue(creditPage(cardOne).inputInvalidFillData());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongOwner.cvs", numLinesToSkip = 1)
    void shouldNotConfirmCreditWithInvalidOwner(String owner) {
        cardOne.setOwner(owner);
        assertTrue(creditPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной информации в поле владелец, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidOwnerUpdatedForCredit() {
        cardOne.setOwner("");
        val creditPage = openStartPage().paymentPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFillData());
        cardOne.setOwner(setFakeOwner());
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с полем cvc/cvv при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCvc.cvs", numLinesToSkip = 1)
    void shouldNotConfirmPaymentWithInvalidCvc(String cvc) {
        cardOne.setCvc(cvc);
        assertTrue(paymentPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Оплата. После ввода валидной информации cvc/cvv, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidCvcUpdatedForPayment() {
        cardOne.setCvc("");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidFillData());
        cardOne.setCvc(randomCvc());
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Негативные сценарии с полем cvc/cvv при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCvc.cvs", numLinesToSkip = 1)
    void shouldNotConfirmCreditWithInvalidCvc(String cvc) {
        cardOne.setCvc(cvc);
        assertTrue(creditPage(cardOne).inputInvalidFormat());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной информации cvc/cvv, предупреждающая надпись исчезает")
    void shouldNotShowWarningIfValidCvcUpdatedForCredit() {
        cardOne.setCvc("");
        val creditPage = openStartPage().paymentPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFillData());
        cardOne.setCvc(randomCvc());
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        // добавить проверку БД
    }

    // Дополнительные методы

    public StartPage openStartPage() {
        open("http://localhost:8080/");
        val startPage = new StartPage();
        return startPage;
    }

    public PaymentPage paymentPage(Card card) {
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(card);
        return paymentPage;
    }

    public CreditPage creditPage(Card card) {
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(card);
        return creditPage;
    }

    public void setCards() {
        cardOne.setNumber("4444 4444 4444 4441");
        cardTwo.setNumber("4444 4444 4444 4442");
        invalidNumberCard.setNumber("4444 4444 4444 4444");
        cardOne.setMonth("01");
        cardTwo.setMonth("01");
        cardOne.setYear(setCorrectYear());
        cardTwo.setYear(setCorrectYear());
        cardOne.setOwner(setFakeOwner());
        cardTwo.setOwner(setFakeOwner());
        cardOne.setCvc(randomCvc());
        cardTwo.setCvc(randomCvc());
    }

    public String setCorrectYear() {
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

    public String setFakeOwner() {
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
