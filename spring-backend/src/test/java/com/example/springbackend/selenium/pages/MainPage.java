package com.example.springbackend.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MainPage {
    WebDriver driver;
    @FindBy(xpath = "//*[@id='map']")
    WebElement map;
    @FindBy(xpath = "//*[@id='side-panel-icon']")
    WebElement sidePanelIcon;
    @FindBy(xpath = "//input[@placeholder='Add the starting point']")
    WebElement startingPoint;

    @FindBy(xpath = "//button[contains(text(),'ORDER RIDE')]")
    WebElement orderButton;

    @FindBy(xpath = "//*[@id='hamburger-menu']")
    WebElement hamburgerMenu;

    String destinationXpath = "//input[@placeholder='Add a destination']";

    public MainPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean isPageOpened(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='map']")));

        return true;
    }

    public void fillStartingPoint(String address){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(startingPoint)).click();
        startingPoint.sendKeys(address);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-icon='plus']"))).click();
    }

    public void fillDestinationPoint(String address){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(destinationXpath))).click();
        driver.findElement(By.xpath(destinationXpath)).sendKeys(address);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@data-icon='plus']"))).click();
    }

    public void openSidePanel() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='side-panel-icon']"))).click();
    }

    public void orderRide(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(orderButton)).click();
    }

    public void acceptRide(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'BEGIN RIDE')]"))).click();
    }
    public void rejectRide(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'REJECT RIDE')]"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//textarea[@placeholder='Please enter the reason for rejecting this ride...']"))).click();

        driver.findElement(By.xpath("//textarea[@placeholder='Please enter the reason for rejecting this ride...']")).sendKeys("RandomReason");


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'Reject ride')]"))).click();
    }

    public void completeRide() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(180));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'COMPLETE RIDE')]"))).click();
    }

    public void viewRideRejectionRequests(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(hamburgerMenu)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(),'View ride rejection requests')]"))).click();
    }

    public boolean expectMessage(String message){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(., '"+message+"')]")));
        }
        catch (NoSuchElementException ex){
            return false;
        }
        return true;
    }
    public boolean isRideRejectedPassenger(){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(., 'The driver rejected the ride.')]")));
        }
        catch (NoSuchElementException ex){
            return false;
        }
        return true;
    }
    public boolean adequateDriverNotFound(){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(., 'Adequate driver not found.')]")));
        }
        catch (NoSuchElementException ex){
            return false;
        }
        return true;
    }

    public boolean insufficientFunds() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(., 'Insufficient funds.')]")));
        }
        catch (NoSuchElementException ex){
            return false;
        }
        return true;
    }

    public void addPassenger(String passengerEmail) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(., 'Link more passengers')]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[contains(@placeholder,'Enter a co-passenger')]"))).click();
        driver.findElement(By.xpath("//input[contains(@placeholder,'Enter a co-passenger')]")).sendKeys(passengerEmail);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='addPassengerPlus']"))).click();
    }

    public void confirmSplitFareRide() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'Confirm')]"))).click();
    }

    public void fillRideInAdvance(int minutes) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='order-in-advance']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='delay-ride-minutes']"))).click();
        driver.findElement(By.xpath("//*[@id='delay-ride-minutes']")).sendKeys(String.valueOf(minutes));

    }

    public void rejectSplitFareRide() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'Reject')]"))).click();
    }

    public boolean expectCancelledRide() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(90));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(., 'The ride is cancelled because one of the')]")));
        }
        catch (NoSuchElementException ex){
            return false;
        }
        return true;
    }
}
