package com.example.springbackend.selenium.pages.Login;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    public static String URL="http://localhost:4200/auth/login";

    WebDriver driver;
    @FindBy(xpath = "//*[@id='login-email']")
    WebElement email;
    @FindBy(xpath = "//*[@id='login-password']")
    WebElement password;
    @FindBy(xpath = "//*[@id='login-button']")
    WebElement loginButton;
    @FindBy(xpath = "//*[@id='login-header']")
    WebElement loginHeader;
    @FindBy(xpath = "//a[contains(@href, 'http://localhost:8080/oauth2/authorization/google')]")
    WebElement googleLoginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        driver.get(URL);
        PageFactory.initElements(driver, this);
    }

    public void login(String emailString, String passwordString) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions action = new Actions(driver);

        wait.until(ExpectedConditions.elementToBeClickable(email)).click();
        action.moveToElement(email).click().build().perform();
        email.sendKeys(emailString);

        wait.until(ExpectedConditions.elementToBeClickable(password)).click();
        password.sendKeys(passwordString);

        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }
    public void incorrectPasswordLogin() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions action = new Actions(driver);

        wait.until(ExpectedConditions.elementToBeClickable(email)).click();
        action.moveToElement(email).click().build().perform();
        email.sendKeys("passenger1@noemail.com");

        wait.until(ExpectedConditions.elementToBeClickable(password)).click();
        password.sendKeys("cascaded123");

        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }
    public void incorrectEmailLogin() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions action = new Actions(driver);

        wait.until(ExpectedConditions.elementToBeClickable(email)).click();
        action.moveToElement(email).click().build().perform();
        email.sendKeys("passenger12356@noemail.com");

        wait.until(ExpectedConditions.elementToBeClickable(password)).click();
        password.sendKeys("cascaded");

        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public boolean checkIncorrectCredentials(){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text() = ' Incorrect credentials. ']")));
        }
        catch (org.openqa.selenium.TimeoutException ex){
            return false;
        }
        return true;
    }

    public void goToGoogleLogin(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOf(googleLoginButton)).click();
    }

    public boolean isPageOpened(){
        boolean isOpened = (new WebDriverWait(driver, Duration.ofSeconds(10)))
                .until(ExpectedConditions.textToBePresentInElement(loginHeader, "Log in"));
        return isOpened;
    }
}
