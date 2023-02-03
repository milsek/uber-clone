package com.example.springbackend.selenium.tests;

import com.example.springbackend.SpringBackendApplication;
import com.example.springbackend.selenium.pages.Login.LoginPage;
import com.example.springbackend.selenium.pages.MainPage;
import com.example.springbackend.selenium.pages.RideRejectionPage;
import com.example.springbackend.service.TestDataSupplierService;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = SpringBackendApplication.class)
@Transactional
public class RideTests {

    WebDriver driver;
    ChromeOptions chromeOptions = new ChromeOptions();
    @Autowired
    TestDataSupplierService testDataSupplierService;

    @BeforeEach()
    public void setupAll() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterEach()
    public void setupQuitAll() {
        driver.quit();
    }


    public void resetDatabase() {
        testDataSupplierService.resetRides();
    }


    @Test
    public void mainBasicRideTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger5@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Ivana Boldižara 39, Novi Sad");
        mainPage.fillDestinationPoint("Novosadski put 162, Novi Sad");
        mainPage.orderRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePage2 = new LoginPage(driver2);
        assertTrue(homePage2.isPageOpened());
        homePage2.login("driver6@noemail.com","cascaded");
        MainPage mainPage2 = new MainPage(driver2);
        assertTrue(mainPage2.isPageOpened());
        mainPage2.acceptRide();
        mainPage2.completeRide();
        driver2.quit();
    }


    @Test
    public void rejectRideTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Drvarska 8 Novi Sad");
        mainPage.fillDestinationPoint("Futoški put 93 Novi Sad");
        mainPage.orderRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePage2 = new LoginPage(driver2);
        assertTrue(homePage2.isPageOpened());
        homePage2.login("driver1@noemail.com","cascaded");
        MainPage mainPage2 = new MainPage(driver2);
        assertTrue(mainPage2.isPageOpened());
        mainPage2.rejectRide();

        WebDriver driver3 = new ChromeDriver(this.chromeOptions);
        driver3.manage().window().maximize();
        LoginPage adminLoginPage = new LoginPage(driver3);
        assertTrue(adminLoginPage.isPageOpened());
        adminLoginPage.login("admin1@noemail.com","cascaded");
        MainPage adminMainPage = new MainPage(driver3);
        assertTrue(adminMainPage.isPageOpened());
        adminMainPage.viewRideRejectionRequests();
        RideRejectionPage rideRejectionPage = new RideRejectionPage(driver3);
        rideRejectionPage.acceptFirstRide();

        assertTrue(mainPage.expectMessage("The driver rejected the ride."));
        assertTrue(mainPage2.expectMessage("Your rejection is accepted"));
        driver2.quit();
        driver3.quit();
    }

    @Test
    public void noAvailableDriversTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Kraljice Natalije 19 Beograd");
        mainPage.fillDestinationPoint("Mutapova 5-7 Beograd");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("Adequate driver not found."));
    }
    @Test
    public void insufficientFundsTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Suvoborska 16 Novi Sad");
        mainPage.fillDestinationPoint("Svetogorska 3 Novi Sad");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("Insufficient funds."));
    }
    @Test
    public void minimumDistanceTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Futoska 10 Novi Sad");
        mainPage.fillDestinationPoint("Futoska 12 Novi Sad");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("Minimum ride distance is 0.25km"));
    }
    @Test
    public void maximumDistanceTest() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger2@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Suvoborska 16 Novi Sad");
        mainPage.fillDestinationPoint("Momačka 9 Mali Mokri Lug");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("Maximum ride distance is 100km"));
    }

    @Test
    public void splitFareRideTest(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger3@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Vladimira Perića Valtera Novi Sad");
        mainPage.fillDestinationPoint("Dr Ilije Đuričića 1 Novi Sad");
        mainPage.addPassenger("passenger2@noemail.com");
        mainPage.orderRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePageUser2 = new LoginPage(driver2);
        assertTrue(homePageUser2.isPageOpened());
        homePageUser2.login("passenger2@noemail.com","cascaded");

        MainPage mainPageUser2 = new MainPage(driver2);
        assertTrue(mainPageUser2.isPageOpened());
        mainPageUser2.confirmSplitFareRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        WebDriver driver3 = new ChromeDriver(this.chromeOptions);
        driver3.manage().window().maximize();


        LoginPage homePage2 = new LoginPage(driver3);
        assertTrue(homePage2.isPageOpened());
        homePage2.login("driver2@noemail.com","cascaded");
        MainPage mainPage2 = new MainPage(driver3);
        assertTrue(mainPage2.isPageOpened());
        mainPage2.acceptRide();
        mainPage2.completeRide();
        driver2.quit();
        driver3.quit();
    }


    @Test
    public void splitFareRideInsufficientFundsTest(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Novosadski put 2");
        mainPage.fillDestinationPoint("Ivana Gorana Kovacica 23");
        mainPage.addPassenger("passenger2@noemail.com");
        mainPage.orderRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePageUser2 = new LoginPage(driver2);
        assertTrue(homePageUser2.isPageOpened());
        homePageUser2.login("passenger2@noemail.com","cascaded");

        MainPage mainPageUser2 = new MainPage(driver2);
        assertTrue(mainPageUser2.isPageOpened());
        mainPageUser2.confirmSplitFareRide();
        assertTrue(mainPageUser2.expectMessage("Ride is cancelled due to insufficient funds."));
        driver2.quit();
    }

    @Test
    public void splitFareRideNonExistentEmail(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Novosadski put 2");
        mainPage.fillDestinationPoint("Ivana Gorana Kovacica 23");
        mainPage.addPassenger("unknown@noemail.com");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("email does not exist in the system"));
    }

    @Test
    public void splitFareRideDriverEmail() {
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Novosadski put 2");
        mainPage.fillDestinationPoint("Ivana Gorana Kovacica 23");
        mainPage.addPassenger("driver1@noemail.com");
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("email does not exist in the system"));
    }

    @Test
    public void splitFareRidePassengerRejectRide(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger3@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Bulevar Patrijarha Pavla 36 Novi Sad");
        mainPage.fillDestinationPoint("Kis Ernea 8, Telep Novi Sad");
        mainPage.addPassenger("passenger5@noemail.com");
        mainPage.orderRide();


        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePageUser2 = new LoginPage(driver2);
        assertTrue(homePageUser2.isPageOpened());
        homePageUser2.login("passenger5@noemail.com","cascaded");

        MainPage mainPageUser2 = new MainPage(driver2);
        assertTrue(mainPageUser2.isPageOpened());
        mainPageUser2.rejectSplitFareRide();
        assertTrue(mainPage.expectMessage("A passenger has rejected the ride."));
        driver2.quit();
    }


    @Test
    public void splitFareRidePassengerNotRespond(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger5@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Bulevar Patrijarha Pavla 36 Novi Sad");
        mainPage.fillDestinationPoint("Kis Ernea 8, Telep Novi Sad");
        mainPage.addPassenger("passenger4@noemail.com");
        mainPage.orderRide();
        assertTrue(mainPage.expectCancelledRide());
    }

    @Test
    public void scheduleRide(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger4@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Bulevar Slobodana Jovanovića 48 Novi Sad");
        mainPage.fillDestinationPoint("Bulevar Jovana Dučića 1 Novi Sad");
        mainPage.fillRideInAdvance(20);
        mainPage.orderRide();

        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        WebDriver driver2 = new ChromeDriver(this.chromeOptions);
        driver2.manage().window().maximize();


        LoginPage homePage2 = new LoginPage(driver2);
        assertTrue(homePage2.isPageOpened());
        homePage2.login("driver5@noemail.com","cascaded");
        MainPage mainPage2 = new MainPage(driver2);
        assertTrue(mainPage2.isPageOpened());
        mainPage2.acceptRide();
        mainPage2.completeRide();
        driver2.quit();
    }
    @Test
    public void scheduleRideTooSoon(){
        LoginPage homePage = new LoginPage(driver);
        assertTrue(homePage.isPageOpened());
        homePage.login("passenger1@noemail.com","cascaded");

        MainPage mainPage = new MainPage(driver);
        assertTrue(mainPage.isPageOpened());
        mainPage.openSidePanel();
        mainPage.fillStartingPoint("Bulevar Patrijarha Pavla 36 Novi Sad");
        mainPage.fillDestinationPoint("Kis Ernea 8, Telep Novi Sad");
        mainPage.fillRideInAdvance(7);
        mainPage.orderRide();
        assertTrue(mainPage.expectMessage("Reservation must be made"));
    }

}
