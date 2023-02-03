import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule, By } from '@angular/platform-browser';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { PassengerService } from 'src/app/core/http/user/passenger.service';
import { PassengerSearchResult } from 'src/app/shared/models/passenger.model';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  
  const passengerServiceSpy = jasmine.createSpyObj<PassengerService>(['register']);
  let fixture: ComponentFixture<RegisterComponent>;
  let faChevronLeft: IconDefinition;
  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        RegisterComponent
      ],
      providers: [
        { provide: PassengerService, useValue: passengerServiceSpy },
      ],
      imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule
      ],
    }).compileComponents();
    
    fixture = TestBed.createComponent(RegisterComponent);
  });

  it('should create the app', () => {
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  /*
    app.loginForm.patchValue({
      email: "",
      password: ""
    });*/
  
    it("should display email required message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.email.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#email-required'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    it("should display email invalid message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.email.setValue("randomEmail")
      app.registerForm.controls.email.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#email-invalid'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display name required message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.name.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#name-required'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display name too short message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.name.setValue("1")
      app.registerForm.controls.name.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#name-too-short'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display name too long message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.name.setValue("asd56f4as56df4asdf564as6d5f4as65df4a65sd4f65a4a654sd6f4asdf54as6df4")
      app.registerForm.controls.name.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#name-too-long'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display surname required message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.surname.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#surname-required'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display surname too short message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.surname.setValue("1")
      app.registerForm.controls.surname.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#surname-too-short'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display surname too long message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.surname.setValue("asd56f4as56df4asdf564as6d5f4as65df4a65sd4f65a4a654sd6f4asdf54as6df4")
      app.registerForm.controls.surname.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#surname-too-long'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display name and surname too long message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.surname.setValue("asd56f4as56df4asdf564as6d5f4as65df4a65sd4f65a4a654sd6f4asdf54as6df4")
      app.registerForm.controls.surname.markAsTouched();
      app.registerForm.controls.name.setValue("asd56f4as56df4asdf564as6d5f4as65df4a65sd4f65a4a654sd6f4asdf54as6df4")
      app.registerForm.controls.name.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#surname-too-long'));
      expect(emailRequired).toBeTruthy();
      const nameTooLong = fixture.debugElement.query(By.css('#name-too-long'));
      expect(nameTooLong).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display phoneNumber required message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.phoneNumber.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#phoneNumber-required'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));


    
    it("should display phoneNumber invalid message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.phoneNumber.setValue("qweqw")
      app.registerForm.controls.phoneNumber.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#phoneNumber-invalid'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));


    
    it("should display password different message", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.password.setValue("xc4bzdx56b")
      app.registerForm.controls.password.markAsTouched();
      app.registerForm.controls.confirmPassword.setValue("35dz4fg5sd43afg")
      app.registerForm.controls.confirmPassword.markAsTouched();
      fixture.detectChanges();
  
      const emailRequired = fixture.debugElement.query(By.css('#passwords-different'));
      expect(emailRequired).toBeTruthy();
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should display password too short", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.email.setValue("test@test.com");
      app.registerForm.controls.email.markAsTouched();
      app.registerForm.controls.name.setValue("qweqw");
      app.registerForm.controls.name.markAsTouched();
      app.registerForm.controls.surname.setValue("qweqw");
      app.registerForm.controls.surname.markAsTouched();
      app.registerForm.controls.city.setValue("qweqw");
      app.registerForm.controls.city.markAsTouched();
      app.registerForm.controls.phoneNumber.setValue("1321231321");
      app.registerForm.controls.phoneNumber.markAsTouched();
      app.registerForm.controls.password.setValue("ewq");
      app.registerForm.controls.password.markAsTouched();
      app.registerForm.controls.confirmPassword.setValue("qweqweqwe");
      app.registerForm.controls.confirmPassword.markAsTouched();
      
      fixture.detectChanges();

      
      const emailRequired = fixture.debugElement.query(By.css('#password-too-short'));
      expect(emailRequired).toBeTruthy();
  
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();
      fixture.detectChanges();
  

      expect(passengerServiceSpy.register).not.toHaveBeenCalled();
    }));

    
    it("should call register", fakeAsync( () => {
      passengerServiceSpy.register.calls.reset();
      const app = fixture.componentInstance;
      app.registerForm.controls.email.setValue("test@test.com");
      app.registerForm.controls.email.markAsTouched();
      app.registerForm.controls.name.setValue("qweqw");
      app.registerForm.controls.name.markAsTouched();
      app.registerForm.controls.surname.setValue("qweqw");
      app.registerForm.controls.surname.markAsTouched();
      app.registerForm.controls.city.setValue("qweqw");
      app.registerForm.controls.city.markAsTouched();
      app.registerForm.controls.phoneNumber.setValue("1321231321");
      app.registerForm.controls.phoneNumber.markAsTouched();
      app.registerForm.controls.password.setValue("qweqweqwe");
      app.registerForm.controls.password.markAsTouched();
      app.registerForm.controls.confirmPassword.setValue("qweqweqwe");
      app.registerForm.controls.confirmPassword.markAsTouched();
      
      fixture.detectChanges();
  
      
      const loginButton = fixture.debugElement.query(By.css('#register-button'));
      loginButton.nativeElement.click();
      fixture.detectChanges();
  

      expect(passengerServiceSpy.register).toHaveBeenCalled();
    }));


  
/*
  it('should render title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.content span')?.textContent).toContain('frontend app is running!');
  });

  */
});
