## План автоматизации

### Перечень автоматизируемых сценариев: 

* Валидные данные для успешного прохождения операции:
1. Номер карты = 4444 4444 4444 4441
1. Месяц = 01
1. Год = генерируется с помощью метода, всегда выставляющего год на 2 позже текущего
1. Владелец = генерируются латинские имя и фамилия с помощью faker
1. CVC = генерируется с помощью отдельного метода из списка номеров 

### Тестируемые Базы данных
1. MySql
1. PostgreSQL 

* Во всех тестовых случаях, требующих получения ответа из БД, должны поочередно быть протестированы обе указанных БД
* В случаях необходимости проверить запись в БД следует сверяться с алгоритмом заполнения таблиц, приведенным ниже
* Для любых карт, не являющихся тестовыми (валидная 4441 и невалидная 4442) БД при отправке запроса заполняться не должна

##### Алгоритм заполнения таблиц БД: 
* В процессе тестирования заполняются следующие таблицы:
    * order_entity заполняется в случае любого запроса. Включает в себя зашифрованный id карты, дату и время создания запроса, credit_id, payment_id. Credit_id заполняется только в случае запроса на кредит, payment_id в случае запроса на оплату. 
    * payment_entity заполняется в случае запроса на оплату. Включает в себя зашифрованный id карты, сумму оплаты, дату создания запроса, статус запроса, transaction_id
    * credit_request_entity заполняется в случае запроса на кредит. Включает в себя зашифрованный id карты, bank_id, дату создания запроса, статус запроса.
* Ожидаемый результат в случае операции "Оплата" 
    * для карты 4444 4444 4444 4441 в таблице payment_entity появилась запись со статусом APPROVED, все поля заполнены значениями, в таблице order_entity появляется запись, все поля кроме credit_id заполнены значениями, поле credit_id = NULL. 
    * для карты 4444 4444 4444 4442 в таблице payment_entity появилась запись со статусом DECLINED, все поля заполнены значениями. В таблице order_entity появляется запись, все поля кроме credit_id заполнены значениями, поле credit_id = NULL. 
* Ожидаемый результат в случае операции "Кредит"
    * для карты 4444 4444 4444 4441 в таблице credit_request_entity появляется запись, все поля заполнены, статус APPROVED. В таблице order_entity появляется запись, в поле credit_id появляется значение, в поле payment_id значение = NULL
    * для карты 4444 4444 4444 4442 в таблице credit_request_entity появляется запись, все поля заполнены, статус DECLINED, в поле credit_id появляется значение, в поле payment_id значение = NULL (требует уточнения спека - должен ли создаваться credit_id, если кредит не одобрен)

### Тестовые сценарии

#### Купить - успешная операция
1. на странице localhost:8080 нажать "Купить"
1. функция "Оплата по карте" появилась
1. ввести номер карты 4444 4444 4444 4441
1. ввести месяц 01
1. ввести год 
1. владелец генерируется с помощью faker 
1. cvc/cvv генерируется с помощью метода
1. нажать "продолжить" 
1. получить результат - всплывающее окно "Успешно! Операция одобрена банком" 
1. проверить, что в БД появилась запись в order_id, в payment_id появилась запись со статусом Approved. 

#### Купить в кредит - успешная операция
1. на странице localhost:8080 нажать "Купить в кредит" 
1. функция "Кредит по данным карты" появилась
1. ввести номер карты 4444 4444 4444 4441
1. ввести месяц 01
1. ввести год 
1. владелец генерируется с помощью faker 
1. cvc/cvv генерируется с помощью метода
1. нажать "продолжить" 
1. получить результат - всплывающее окно "Успешно! Операция одобрена банком" 
1. проверить, что в БД появилась запись в order_id, в payment_id появилась запись со статусом Approved. 

#### Купить - неуспешная операция
1. на странице localhost:8080 нажать "Купить"
1. функция "Оплата по карте" появилась
1. ввести номер карты 4444 4444 4444 4442
1. ввести месяц 01
1. ввести год 
1. владелец генерируется с помощью faker 
1. cvc/cvv генерируется с помощью метода
1. нажать "продолжить" 
1. получить результат - всплывающее окно "Ошибка! Банк отказал в проведении операции" 
1. проверить, что в БД появилась запись в order_id, не появилась запись в таблице payment_id. 

#### Купить в кредит - неуспешная операция
1. на странице localhost:8080 нажать "Купить в кредит" 
1. функция "Кредит по данным карты" появилась
1. ввести номер карты 4444 4444 4444 4442
1. ввести месяц 01
1. ввести год 
1. владелец генерируется с помощью faker 
1. cvc/cvv генерируется с помощью метода
1. нажать "продолжить" 
1. получить результат - всплывающее окно "Ошибка! Банк отказал в проведении операции" 
1. проверить, что в БД появилась запись Declined. 

#### Купить. Негативные сценарии с номером карты: 
##### Оплата не происходит, если строку с номером карты оставить пустой
* ввести все валидные данные 
* строку номер карты оставить пустой. 
##### Оплата не происходит, если в строке номер карты меньше чисел
* ввести все валидные данные
* строку номер карты заполнить меньшим количеством знаков "4444 4444 44". 
##### Оплата не происходит, если строку номер карты заполнить 0
* ввести все валидные данные
* строку номер карты заполнить нулями. 

Во всех случаях получить результат: сообщение "Неверный формат" под строкой номер карты. Строка выделена красным. 

##### Оплата не происходит, если неверен номер карты
* ввести все валидные данные
* номер карты "4444 4444 4444 4444". 
* Получить сообщение "Неверный номер карты". Строка выделена красным. 

##### После ввода валидного номера карты, предупреждающая надпись исчезает 
* ввести все валидные данные
* строку номер карты заполнить меньшим количеством знаков "4444 4444 44". 
* нажать "продолжить"
* убедиться, что предупреждающая надпись появилась
* вести валидный номер карты
* нажать "продолжить"
* убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись.  

#### Купить в кредит. Негативные сценарии с номером карты: 
##### Оплата не происходит, если строку с номером карты оставить пустой
* ввести все валидные данные 
* строку номер карты оставить пустой. 
##### Оплата не происходит, если в строке номер карты меньше чисел
* ввести все валидные данные
* строку номер карты заполнить меньшим количеством знаков "4444 4444 44". 
##### Оплата не происходит, если строку номер карты заполнить 0
* ввести все валидные данные
* строку номер карты заполнить нулями. 

Во всех случаях получить результат: сообщение "Неверный формат" под строкой номер карты. Строка выделена красным. 

##### Оплата не происходит, если неверен номер карты
* ввести все валидные данные
* номер карты "4444 4444 4444 4444". 
* Получить сообщение "Неверный номер карты". Строка выделена красным. 

##### После ввода валидного номера карты, предупреждающая надпись исчезает 
* ввести все валидные данные
* строку номер карты заполнить меньшим количеством знаков "4444 4444 44". 
* нажать "продолжить"
* убедиться, что предупреждающая надпись появилась
* вести валидный номер карты
* нажать "продолжить"
* убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись.   

#### Купить. Негативные сценарии с датой:
##### Оплата не происходит, если строку с месяцем оставить пустой
* ввести все валидные данные
* строку с месяцем оставить пустой. 
* Получить сообщение "Неверный формат" под строкой месяц. Строка выделена красным. 
##### Оплата не происходит, если месяц обозначен одной цифрой
* ввести все валидные данные
* строку с месяцем заполнить цифрой 1. 
* Получить сообщение "Неверный формат" под строкой месяц. Строка выделена красным. 
##### Оплата не происходит, если указан несуществующий месяц
* ввести все валидные данные
* в строке месяц проставить "22"
* Получить сообщение "Неверно указан срок действия карты". Строка выделена красным.
##### Оплата не происходит, если строку с годом оставить пустой
* ввести все валидные данные, 
* строку с годом оставить пустой. 
* Получить сообщение "Неверный формат" под строкой год. Строка выделена красным. 
##### Оплата не происходит, если указан год ранее текущего
* ввести все валидные данные, 
* строку с годом заполнить датой на 2 года ранее (генерируется методом)
* Получить сообщение "Истёк срок действия карты". Строка выделена красным. 
##### После ввода валидных месяца и года, предупреждающая надпись исчезает
* ввести все валидные данные
* строку с месяцем и годом оставить пустой. 
* нажать "продолжить"
* Получить сообщение "Неверный формат" под строкой месяц и год. Строки выделены красным. 
* ввести валидный месяц и год. 
* нажать "продолжить". 
* Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись.  

#### Купить в кредит. Негативные сценарии с датой:
##### Оплата не происходит, если строку с месяцем оставить пустой
* ввести все валидные данные
* строку с месяцем оставить пустой. 
* Получить сообщение "Неверный формат" под строкой месяц. Строка выделена красным. 
##### Оплата не происходит, если месяц обозначен одной цифрой
* ввести все валидные данные
* строку с месяцем заполнить цифрой 1. 
* Получить сообщение "Неверный формат" под строкой месяц. Строка выделена красным. 
##### Оплата не происходит, если указан несуществующий месяц
* ввести все валидные данные
* в строке месяц проставить "22"
* Получить сообщение "Неверно указан срок действия карты". Строка выделена красным.
##### Оплата не происходит, если строку с годом оставить пустой
* ввести все валидные данные, 
* строку с годом оставить пустой. 
* Получить сообщение "Неверный формат" под строкой год. Строка выделена красным. 
##### Оплата не происходит, если указан год ранее текущего
* ввести все валидные данные, 
* строку с годом заполнить датой на 2 года ранее (генерируется методом)
* Получить сообщение "Истёк срок действия карты". Строка выделена красным. 
##### После ввода валидных месяца и года, предупреждающая надпись исчезает
* ввести все валидные данные
* строку с месяцем и годом оставить пустой. 
* нажать "продолжить"
* Получить сообщение "Неверный формат" под строкой месяц и год. Строки выделены красным. 
* ввести валидный месяц и год. 
* нажать "продолжить". 
* Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись.  

#### Купить. Негативные сценарии с полем владелец:
##### Оплата не происходит, если поле владелец не заполнено
* ввести все валидные данные
* строку владелец оставить пустой. 
* Получить сообщение "Поле обязательно для заполнения" под строкой владелец. Строка выделена красным.
##### Оплата не происходит, если ввести имя владельца на русском
* ввести все валидные данные
* ввести имя и фамилию на русском Иванов Иван. 
* Получить сообщение "Неверный формат" под строкой Владелец. Строка  выделена красным.
##### Оплата не происходит, если ввести цифры и знаки в поле владелец
* ввести все валидные данные
* ввести знаки и цифры "№68*)12" в поле владелец. 
* Получить сообщение "Неверный формат" под строкой Владелец. Строка выделена красным.
##### После ввода валидных данных в поле владелец, предупреждающая надпись исчезает
* ввести все валидные данные, строку владелец оставить пустой. 
* нажать "продолжить"
* Получить сообщение "Поле обязательно для заполнения" под строкой владелец. Строка выделена красным. 
* Ввести валидные данные владельца. 
* Нажать "продолжить". Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись. 

#### Купить в кредит. Негативные сценарии с полем владелец:
##### Оплата не происходит, если поле владелец не заполнено
* ввести все валидные данные
* строку владелец оставить пустой. 
* Получить сообщение "Поле обязательно для заполнения" под строкой владелец. Строка выделена красным.
##### Оплата не происходит, если ввести имя владельца на русском
* ввести все валидные данные
* ввести имя и фамилию на русском Иванов Иван. 
* Получить сообщение "Неверный формат" под строкой Владелец. Строка  выделена красным.
##### Оплата не происходит, если ввести цифры и знаки в поле владелец
* ввести все валидные данные
* ввести знаки и цифры "№68*)12"  в поле владелец. 
* Получить сообщение "Неверный формат" под строкой Владелец. Строка выделена красным.
##### После ввода валидных данных в поле владелец, предупреждающая надпись исчезает
* ввести все валидные данные, строку владелец оставить пустой. 
* нажать "продолжить"
* Получить сообщение "Поле обязательно для заполнения" под строкой владелец. Строка выделена красным. 
* Ввести валидные данные владельца. 
* Нажать "продолжить". Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись. 

#### Купить. Негативные сценарии с данными cvc/cvv
##### Оплата не происходит, если строка cvc/cvv не заполнена
* ввести все валидные данные
* строку cvc/cvv оставить пустой. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным. Под строкой владелец не появилось сообщения "Поле обязательно для заполнения" 
##### Оплата не происходит, если в строке cvc/cvv одна цифра
* ввести все валидные данные
* в строку cvc/cvv ввести 1. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным.
##### Оплата не происходит, если в строке cvc/cvv две цифры
* ввести все валидные данные
* в строку cvc/cvv ввести 11. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным.
##### При вводе валидных данных в поле cvc/cvv, предупреждающая надпись исчезает
* ввести все валидные данные
* в строку cvc/cvv ввести 11. 
* нажать "продолжить"
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным. 
* Ввести валидные данные в строку cvc/cvv. 
* Нажать "продолжить". 
* Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись. 

#### Купить в кредит. Негативные сценарии с данными cvc/cvv
1. Оплата не происходит, если строка cvc/cvv не заполнена
* ввести все валидные данные
* строку cvc/cvv оставить пустой. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным. Под строкой владелец не появилось сообщения "Поле обязательно для заполнения" 
##### Оплата не происходит, если в строке cvc/cvv одна цифра
* ввести все валидные данные
* в строку cvc/cvv ввести 1. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным.
##### Оплата не происходит, если в строке cvc/cvv две цифры
* ввести все валидные данные
* в строку cvc/cvv ввести 11. 
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным.
##### При вводе валидных данных в поле cvc/cvv, предупреждающая надпись исчезает
* ввести все валидные данные
* в строку cvc/cvv ввести 11. 
* нажать "продолжить"
* Получить сообщение "Неверный формат" под строкой cvc/cvv. Строка выделена красным. 
* Ввести валидные данные в строку cvc/cvv. 
* Нажать "продолжить". 
* Убедиться, что предупреждающая об ошибке надпись исчезла, операция прошла успешно (всплывающее окно "Успешно! Операция одобрена банком"). В БД появилась соответствующая запись. 

### Перечень используемых инструментов с обоснованием выбора
1. IDEA для написания тестов, как одна из наиболее распространенных в разработке, удобная для работы (не требует установки дополнений, комфортна в работе, дает возможность подключения различных библиотек для написания тестов)
1. Java 8 как наиболее оптимальная и распространенная версия 
1. Play with docker для "упаковки" готового к тестированию продукта, передачи всех параметров тестируемой среды и создания "виртуальной машины" для тестирования приложения
1. Junit, jupiter 5.5.1 - для создания тестов и параметризированных тестов
1. Selenide т.к имеет более оптимальный синтаксис по сравнению с Selenium и позволяет управлять браузером
1. Faker для быстрой генерации данных, позволяет не привязывать тесты к определенным данным и каждый раз генерировать уникальные данные
1. Allure для построения удобочитаемой единой отчетности по всем тестам со скрин-шотами, подробными описаниями
1. MySQL и PostgreSQL для проверки работоспособности системы в различных БД

### Перечень и описание возможных рисков при автоматизации
1. Сложности в работе с БД и docker 
1. Сложности в корректном запуске SUT и node.js
1. Возможно наличие элементов без уникальных атрибутов
1. Отсутствие спека и описания сценариев поведения системы
1. Отсутствие времени в связи с высокой загрузкой на основной работе

### Интервальная оценка с учетом рисков (в часах)
С учетом рисков - около 60-70 часов. 

### План сдачи работ
1. Написание тестов до 25 декабря
1. Предоставление отчетов до 27 декабря



