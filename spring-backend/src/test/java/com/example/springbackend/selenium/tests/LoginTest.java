package com.example.springbackend.selenium.tests;


import com.example.springbackend.selenium.pages.Login.GoogleEmailPage;
import com.example.springbackend.selenium.pages.Login.LoginPage;
import com.example.springbackend.selenium.pages.MainPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginTest {

    WebDriver driver;

    @BeforeEach()
    public void setupAll() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
    }

    @AfterEach()
    public void setupQuitAll() {
        driver.quit();
    }

    @Test()
    public void passengerLoginTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
    }

    @Test()
    public void driverLoginTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("driver1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
    }

    @Test()
    public void adminLoginTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("admin1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
    }


    @Test()
    public void incorrectEmailTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.incorrectEmailLogin();
        assertTrue(homePage.checkIncorrectCredentials());
    }

    @Test()
    public void incorrectPasswordTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.incorrectPasswordLogin();
        assertTrue(homePage.checkIncorrectCredentials());
    }
/*
    @Test()
    public void googleLoginTest() throws InterruptedException {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.goToGoogleLogin();

        GoogleEmailPage emailPage = new GoogleEmailPage(driver);
        assertTrue(emailPage.isPageOpened());
        emailPage.login();
    }*/
}
