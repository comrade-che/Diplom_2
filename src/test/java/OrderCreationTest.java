import config.Config;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import models.Order;
import models.OrderClient;
import models.User;
import models.UserClient;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class OrderCreationTest {

    private OrderClient orderClient;
    private Order order;

    private UserClient userClient;
    private User user;
    String accessToken;

    @Before
    public void setUp() {
        user = new User().generateUser();
        userClient = new UserClient();
        RestAssured.baseURI = Config.BASE_URL;
        userClient.create(user);

        order = new Order();
        orderClient = new OrderClient();

    }

    @After
    public void tearDown() {
        if (accessToken != null) userClient.delete(accessTokenExtraction(user));
    }

    //Данный метод логинит пользователя и возвращает accessToken
    public String accessTokenExtraction(User user) {
        ValidatableResponse response = userClient.login(user);
        return response.extract().path("accessToken");
    }


    public Order createOrder() {
        ValidatableResponse response = orderClient.getIngredients();
        HashMap<String, ArrayList<String>> ingredientsMap = new HashMap<>();
        ingredientsMap.put("ingredients", response.extract().path("data._id"));

        return new Order(ingredientsMap);
    }

    public Order createBrokenOrder(String broken) {
        ValidatableResponse response = orderClient.getIngredients();
        HashMap<String, ArrayList<String>> ingredientsMap = new HashMap<>();

        ArrayList<String> brokenHash = new ArrayList<>();
        brokenHash.add("qofo1");

        if (broken.equals("empty")) {
            ingredientsMap.put("ingredients11", response.extract().path("data._id"));
        } else if (broken.equals("incorrect hash")) {
            ingredientsMap.put("ingredients", brokenHash);
        }

        return new Order(ingredientsMap);
    }



    @Test
    @DisplayName("Проверка создания заказа с авторизацией")
    public void ShouldBeCreateOrderWithLoginTest() {
        order = createOrder();
        ValidatableResponse response = orderClient.create(order, accessTokenExtraction(user));

        assertEquals("Статус код неверный при создании заказа с авторизацией",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка создания заказа без авторизации")
    public void ShouldBeCreateOrderWithoutLoginTest() {
        order = createOrder();
        ValidatableResponse response = orderClient.create(order, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY2NzMyYThhOWVkMjgwMDAxYjQ0Mjk5NyIsImlhdCI6MTcxODgyMzU3NSwiZXhwIjoxNzE4ODI0Nzc1fQ.fxPA1Qa6djiU23SJzBaYMVgGiHA8p51PZZtN25Pd-sQ");

        assertEquals("Статус код неверный при создании заказа без авторизации",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }


    @Test
    @DisplayName("Проверка создания заказа без ингредиентов")
    public void ShouldBeCreateOrderWithoutIngredientsTest() {
        order = createBrokenOrder("empty");
        ValidatableResponse response = orderClient.create(order, accessTokenExtraction(user));

        assertEquals("Статус код неверный при создании заказа без ингредиентов",
                HttpStatus.SC_BAD_REQUEST, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка создания заказа c невалидным хэшем ингредиентов")
    public void ShouldBeCreateOrderWithIncorrectHashTest() {
        order = createBrokenOrder("incorrect hash");
        ValidatableResponse response = orderClient.create(order, accessTokenExtraction(user));

        assertEquals("Статус код неверный при создании заказа c невалидным хэшем ингредиентовв",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, response.extract().statusCode());

    }

}
