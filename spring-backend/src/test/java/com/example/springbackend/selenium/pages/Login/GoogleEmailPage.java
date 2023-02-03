package com.example.springbackend.selenium.pages.Login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GoogleEmailPage {

    WebDriver driver;


    @FindBy(xpath = "//button[contains(@class, 'VfPpkd-LgbsSe VfPpkd-LgbsSe-OWXEXe-k8QpJ VfPpkd-LgbsSe-OWXEXe-dgl2Hf nCP5yc AjY5Oe DuMIQc LQeN7 qIypjc TrZEUc lw1w4b')]")
    WebElement nextButton;
    @FindBy(xpath = "//input[contains(@type, 'email')]")
    WebElement email;

    public GoogleEmailPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void login() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Actions action = new Actions(driver);

        wait.until(ExpectedConditions.elementToBeClickable(email)).click();
        action.moveToElement(email).click().build().perform();
        email.sendKeys("mrsisatim20@gmail.com");
        wait.until(ExpectedConditions.elementToBeClickable(nextButton)).click();
    }
    public boolean isPageOpened(){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(@class, 'VfPpkd-LgbsSe VfPpkd-LgbsSe-OWXEXe-k8QpJ VfPpkd-LgbsSe-OWXEXe-dgl2Hf nCP5yc AjY5Oe DuMIQc LQeN7 qIypjc TrZEUc lw1w4b')]")));
        }
        catch (org.openqa.selenium.TimeoutException ex){
            return false;
        }
        return true;
    }

}
