import config.Config;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import models.User;
import com.github.javafaker.Faker;
import models.UserClient;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserLoginTest {

    static Faker faker = new Faker();

    private UserClient userClient;
    private User user;
    String accessToken;

    @Before
    public void setUp() {
        user = new User().generateUser();
        userClient = new UserClient();
        RestAssured.baseURI = Config.BASE_URL;
        userClient.create(user);
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

    @Test
    @DisplayName("Проверка логина пользователя")
    public void shouldBeLoginUserTest() {
        ValidatableResponse response = userClient.login(user);

        assertEquals("Статус код неверный при логине пользователя",
                HttpStatus.SC_OK, response.extract().statusCode());

        assertTrue("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка логина пользователя с неверным почтовым адресом")
    public void shouldBeLoginUserWithIncorrectNameTest() {
        user.setEmail("incorrect@mail.com");
        ValidatableResponse response = userClient.login(user);

        assertEquals("Статус код неверный при логине пользователя с неверным почтовым адресом",
                HttpStatus.SC_UNAUTHORIZED, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }

    @Test
    @DisplayName("Проверка логина пользователя с неверным паролем")
    public void shouldBeLoginUserWithIncorrectPasswordTest() {
        user.setPassword(faker.internet().password());
        ValidatableResponse response = userClient.login(user);

        assertEquals("Статус код неверный при логине пользователя с неверным паролем",
                HttpStatus.SC_UNAUTHORIZED, response.extract().statusCode());

        assertFalse("Невалидные данные в теле:", response.extract().path("success"));

    }

}
