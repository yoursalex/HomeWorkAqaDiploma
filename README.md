## Дипломный проект Меньшиковой Александры Дмитриевны

[План автоматизации](https://github.com/yoursalex/HomeWorkAqaDiploma/blob/master/documents/PLAN.md)

[Отчет по итогам тестирования](https://github.com/yoursalex/HomeWorkAqaDiploma/blob/master/documents/REPORT.md)

[Отчет по итогам автоматизации](https://github.com/yoursalex/HomeWorkAqaDiploma/blob/master/documents/SUMMARY.md) 

[Все найденные баги заведены в issues](https://github.com/yoursalex/HomeWorkAqaDiploma/issues) 

* файл docker-compose.yml находится в текущей папке
* файл application.properties находится в корневой папке
* файл aqa-shop.jar находится в папке artifacts 

* Docker. Для запуска MySql, PostgreSQL и Node.js использовать команду 
```
docker-compose up -d --build
```

* aqa-shop.jar 
    * Для запуска под MySQL использовать команду 
    ```
    java -Dspring.datasource.url=jdbc:mysql://localhost:3306/app -jar artifacts/aqa-shop.jar
    ```
    * Для запуска под PostgreSQL использовать команду 
    ```
    java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/app -jar artifacts/aqa-shop.jar
    ```
* Тесты (Gradlew + Allure)
   * Для запуска под MySQL использовать команду 
   ```
   gradlew -Ddb.url=jdbc:mysql://localhost:3306/app clean test allureReport
   ```
   * Для запуска под PostgreSQL использовать команду 
   ```
   gradlew -Ddb.url=jdbc:postgresql://localhost:5432/app clean test allureReport
   ```
* Отчеты (Allure)
   * Для получения отчета использовать команду
   ``` 
   gradlew allureServe
   ``` 
