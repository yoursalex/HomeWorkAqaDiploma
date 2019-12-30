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
import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Должен подтверждать покупку и создавать payment_id при валидных данных и карте со статусом APPROVED")
    void shouldConfirmPaymentWithValidDataCardOne() throws SQLException {
        assertTrue(openAndFillPaymentPage(cardOne).notificationOkIsVisible());
        assertEquals(SQLHelper.findPaymentStatus(), "APPROVED");
        assertNotNull(SQLHelper.findPaymentId());
    }

    @Test
    @DisplayName("Должен подтверждать кредит и создавать credit_id при валидных данных и карте со статусом APPROVED")
    void shouldConfirmCreditWithValidDataCardOne() throws SQLException {
        assertTrue(openAndFillCreditPage(cardOne).notificationOkIsVisible());
        assertEquals(SQLHelper.findCreditStatus(), "APPROVED");
        assertNotNull(SQLHelper.findCreditId());
    }

    @Test
    @DisplayName("Не должен подтверждать покупку и создавать payment_id при использовании карты со статусом DECLINED")
    void shouldNotConfirmPaymentWithInvalidCardTwo() throws SQLException{
        assertTrue(openAndFillPaymentPage(cardTwo).notificationErrorIsVisible());
        assertEquals(SQLHelper.findPaymentStatus(), "DECLINED");
        assertNull(SQLHelper.findPaymentId());
    }

    @Test
    @DisplayName("Не должен подтверждать кредит и создавать credit_id при использовании карты со статусом DECLINED")
    void shouldNotConfirmCreditWithInvalidCardTwo() throws SQLException {
        assertTrue(openAndFillCreditPage(cardTwo).notificationErrorIsVisible());
        assertEquals(SQLHelper.findCreditStatus(), "DECLINED");
        assertNull(SQLHelper.findCreditId());
    }

    // Негативные сценарии с номером карты при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCard.cvs", numLinesToSkip = 1)
    void shouldNotSubmitPaymentWithWrongNumber(String number, String message) throws SQLException {
        cardOne.setNumber(number);
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать покупку при невалидном номере карты")
    void shouldNotSubmitPaymentWithIllegalCard() throws SQLException {
        cardOne.setNumber("4444 4444 4444 4444");
        assertTrue(openAndFillPaymentPage(cardOne).notificationErrorIsVisible());
        assertFalse(SQLHelper.isNotEmpty());
}

    @Test
    @DisplayName("Покупка. После ввода валидного номера карты, предупреждающая надпись исчезает, покупка проходит успешно")
    void shouldNotShowWarningIfValidCardNumberUpdatedForPayment() throws SQLException {
        cardOne.setNumber("4444 4444 44");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFormat());
        cardOne.setNumber("4444 4444 4444 4441");
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findPaymentStatus(), "APPROVED");
        assertNotNull(SQLHelper.findPaymentId());
    }

    // Негативные сценарии с номером карты при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCard.cvs", numLinesToSkip = 1)
    void shouldNotSubmitCreditWithWrongNumber(String number, String message) throws SQLException {
        cardOne.setNumber(number);
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать кредит при невалидном номере карты")
    void shouldNotSubmitCreditWithIllegalCard() throws SQLException{
        cardOne.setNumber("4444 4444 4444 4444");
        assertTrue(openAndFillCreditPage(cardOne).notificationErrorIsVisible());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Кредит. После ввода валидного номера карты, предупреждающая надпись исчезает, кредит подтвержден")
    void shouldNotShowWarningIfValidCardNumberUpdatedForCredit() throws SQLException{
        cardOne.setNumber("4444 4444 44");
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFormat());
        cardOne.setNumber("4444 4444 4444 4441");
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findCreditStatus(), "APPROVED");
        assertNotNull(SQLHelper.findCreditId());
    }

    // Негативные сценарии с датой при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongMonth.cvs", numLinesToSkip = 1)
    void shouldNotSubmitPaymentWithWrongMonth(String month, String message) throws SQLException {
        cardOne.setMonth(month);
        assertTrue((openAndFillPaymentPage(cardOne).inputInvalidFormat()), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать покупку, если введен несуществующий месяц")
    void shouldNotConfirmPaymentWithInvalidMonth() throws SQLException {
        cardOne.setMonth("22");
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidMonth());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать покупку без указания года")
    void shouldNotConfirmPaymentIfEmptyYear() throws SQLException {
        cardOne.setYear("");
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFormat());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать покупку, если год меньше текущего")
    void shouldNotConfirmPaymentWithOldYear() throws SQLException {
        cardOne.setYear(setWrongYear());
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidExpireDate());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Покупка. После ввода валидной даты предупреждающая надпись исчезает, покупка проходит успешно")
    void shouldNotShowWarningIfValidDateUpdatedForPayment() throws SQLException{
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
        assertEquals(SQLHelper.findPaymentStatus(), "APPROVED");
        assertNotNull(SQLHelper.findPaymentId());
    }

    // Негативные сценарии с датой при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongMonth.cvs", numLinesToSkip = 1)
    void shouldNotSubmitCreditWithWrongMonth(String month, String message) throws SQLException{
        cardOne.setMonth(month);
        assertTrue((openAndFillCreditPage(cardOne).inputInvalidFormat()), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать кредит, если введен несуществующий месяц")
    void shouldNotConfirmCreditWithInvalidMonth() throws SQLException{
        cardOne.setMonth("22");
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidMonth());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать кредит без указания года")
    void shouldNotConfirmCreditIfEmptyYear() throws SQLException{
        cardOne.setYear("");
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidFormat());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Не должен подтверждать кредит, если год меньше текущего")
    void shouldNotConfirmCreditWithOldYear() throws SQLException{
        cardOne.setYear(setWrongYear());
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidExpireDate());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной даты предупреждающая надпись исчезает, кредит подтвержден")
    void shouldNotShowWarningIfValidDateUpdatedForCredit() throws SQLException {
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
        assertEquals(SQLHelper.findCreditStatus(), "APPROVED");
        assertNotNull(SQLHelper.findCreditId());
    }

    // Негативные сценарии с полем владелец при покупке:

    @Test
    @DisplayName("Не должен подтверждать покупку без имени владельца")
    void shouldNotConfirmPaymentWithoutOwner() throws SQLException{
        cardOne.setOwner("");
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFillData());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongOwner.cvs", numLinesToSkip = 1)
    void shouldNotConfirmPaymentWithInvalidOwner(String owner, String message) throws SQLException {
        cardOne.setOwner(owner);
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Покупка. После ввода валидной информации в поле владелец, предупреждающая надпись исчезает, покупка проходит успешно")
    void shouldNotShowWarningIfValidOwnerUpdatedForPayment() throws SQLException {
        cardOne.setOwner("");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidFillData());
        cardOne.setOwner(setFakeOwner());
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findPaymentStatus(), "APPROVED");
        assertNotNull(SQLHelper.findPaymentId());
    }

    // Негативные сценарии с полем владелец при кредите:

    @Test
    @DisplayName("Не должен подтверждать кредит без имени владельца")
    void shouldNotConfirmCreditWithoutOwner() throws SQLException{
        cardOne.setOwner("");
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidFillData());
        assertFalse(SQLHelper.isNotEmpty());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongOwner.cvs", numLinesToSkip = 1)
    void shouldNotConfirmCreditWithInvalidOwner(String owner, String message) throws SQLException{
        cardOne.setOwner(owner);
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной информации в поле владелец, предупреждающая надпись исчезает, кредит подтвержден")
    void shouldNotShowWarningIfValidOwnerUpdatedForCredit() throws SQLException {
        cardOne.setOwner("");
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFillData());
        cardOne.setOwner(setFakeOwner());
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findCreditStatus(), "APPROVED");
        assertNotNull(SQLHelper.findCreditId());
    }

    // Негативные сценарии с полем cvc/cvv при оплате:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCvc.cvs", numLinesToSkip = 1)
    void shouldNotConfirmPaymentWithInvalidCvc(String cvc, String message) throws SQLException{
        cardOne.setCvc(cvc);
        assertTrue(openAndFillPaymentPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Покупка. После ввода валидной информации cvc/cvv, предупреждающая надпись исчезает, покупка проходит успешно")
    void shouldNotShowWarningIfValidCvcUpdatedForPayment() throws SQLException {
        cardOne.setCvc("");
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidFillData());
        cardOne.setCvc(randomCvc());
        paymentPage.cleanData();
        paymentPage.fillData(cardOne);
        assertTrue(paymentPage.inputInvalidIsNotVisible());
        assertTrue(paymentPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findPaymentStatus(), "APPROVED");
        assertNotNull(SQLHelper.findPaymentId());
    }

    // Негативные сценарии с полем cvc/cvv при кредите:

    @ParameterizedTest
    @CsvFileSource(resources = "/wrongCvc.cvs", numLinesToSkip = 1)
    void shouldNotConfirmCreditWithInvalidCvc(String cvc, String message) throws SQLException {
        cardOne.setCvc(cvc);
        assertTrue(openAndFillCreditPage(cardOne).inputInvalidFormat(), message);
        assertFalse(SQLHelper.isNotEmpty());
    }

    @Test
    @DisplayName("Кредит. После ввода валидной информации cvc/cvv, предупреждающая надпись исчезает, кредит подтвержден")
    void shouldNotShowWarningIfValidCvcUpdatedForCredit() throws SQLException{
        cardOne.setCvc("");
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidFillData());
        cardOne.setCvc(randomCvc());
        creditPage.cleanData();
        creditPage.fillData(cardOne);
        assertTrue(creditPage.inputInvalidIsNotVisible());
        assertTrue(creditPage.notificationOkIsVisible());
        assertEquals(SQLHelper.findCreditStatus(), "APPROVED");
        assertNotNull(SQLHelper.findCreditId());
    }

    // Дополнительные методы

    private StartPage openStartPage() {
        open("http://localhost:8080/");
        val startPage = new StartPage();
        return startPage;
    }

    private PaymentPage openAndFillPaymentPage(Card card) {
        val paymentPage = openStartPage().paymentPage();
        paymentPage.fillData(card);
        return paymentPage;
    }

    private CreditPage openAndFillCreditPage(Card card) {
        val creditPage = openStartPage().creditPage();
        creditPage.fillData(card);
        return creditPage;
    }

    private void setCards() {
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

    private String setCorrectYear() {
        LocalDate date = LocalDate.now().plusYears(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        String year = date.format(formatter);
        return year;
    }

    private String setWrongYear() {
        LocalDate date = LocalDate.now().minusYears(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        String year = date.format(formatter);
        return year;
    }

   private String setFakeOwner() {
        String owner = faker.name().fullName();
        return owner;
    }

    private String randomCvc() {
        String[] cvcOptions = {"123", "999", "985", "015", "888", "656", "001", "234", "601", "111"};
        int chooseCvc =(int) (Math.random()*cvcOptions.length);
        String cvc = cvcOptions[chooseCvc];
        return cvc;
    }
}
