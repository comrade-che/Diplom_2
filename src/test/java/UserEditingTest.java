import com.github.javafaker.Faker;
import config.Config;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import models.User;
import models.UserClient;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserEditingTest {

    static Faker faker = new Faker();

    private UserClient userClient;
    private User user;
    String accessToken;
    String refreshTokenAfterRegistration;

    @Before
    public void setUp() {
        user = new User().generateUser();
        userClient = new UserClient();
        RestAssured.baseURI = Config.BASE_URL;
        refreshTokenAfterRegistration = refreshTokenExtraction(user);
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

    //Данный метод создает пользователя и возвращает accessToken
    public String refreshTokenExtraction(User user) {
        ValidatableResponse response = userClient.create(user);
        return response.extract().path("refreshToken");
    }

    @Test
    @DisplayName("Проверка изменение почтового адреса авторизованного пользователя")
    public void shouldBeEditEmailUserTest() {
        ValidatableResponse response = userClient.edit(accessTokenExtraction(user),
                new User(faker.internet().emailAddress(), user.getPassword(), user.getName()));

        assertEquals("Статус код неверный при изменение почтового адреса авторизованного пользователя",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка изменение имени авторизованного пользователя")
    public void shouldBeEditNameUserTest() {
        ValidatableResponse response = userClient.edit(accessTokenExtraction(user),
                new User(user.getEmail(), user.getPassword(), faker.name().firstName()));

        assertEquals("Статус код неверный при изменение имени авторизованного пользователя",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка изменение пароля авторизованного пользователя")
    public void shouldBeEditPasswordUserTest() {
        ValidatableResponse response = userClient.edit(accessTokenExtraction(user),
                new User(user.getEmail(), faker.internet().password(), user.getName()));

        assertEquals("Статус код неверный при изменение имени авторизованного пользователя",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка изменение пароля авторизованного пользователя")
    public void shouldBeEditPasswordNonLoginUserTest() {
        accessToken = "";
        ValidatableResponse response = userClient.edit(accessToken,
                new User(user.getEmail(), faker.internet().password(), user.getName()));

        assertEquals("Статус код неверный при изменение имени авторизованного пользователя",
                HttpStatus.SC_UNAUTHORIZED, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка изменение почтового адреса НЕавторизованного пользователя")
    public void shouldBeEditEmailNonLoginUserTest() {
        accessToken = "";
        ValidatableResponse response = userClient.edit(accessToken,
                new User(faker.internet().emailAddress(), user.getPassword(), user.getName()));

        assertEquals("Статус код неверный при изменение почтового адреса Неавторизованного пользователя",
                HttpStatus.SC_UNAUTHORIZED, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка изменение имени НЕавторизованного пользователя")
    public void shouldBeEditNameNonLoginUserTest() {
        accessToken = "";
        ValidatableResponse response = userClient.edit(accessToken,
                new User(user.getEmail(), user.getPassword(), faker.name().firstName()));

        assertEquals("Статус код неверный при изменение имени Неавторизованного пользователя",
                HttpStatus.SC_UNAUTHORIZED, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }
}
