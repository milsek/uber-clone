import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule, By } from '@angular/platform-browser';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  
  const authServiceSpy = jasmine.createSpyObj<AuthenticationService>(['login']);
  let fixture: ComponentFixture<LoginComponent>;
  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        LoginComponent,
      ],
      providers: [
        { provide: AuthenticationService, useValue: authServiceSpy },
      ],
      imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule
      ],
    }).compileComponents();
    
    fixture = TestBed.createComponent(LoginComponent);
  });

  it('should create the app', () => {
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
  
  it("should display email required message", fakeAsync( () => {
    authServiceSpy.login.calls.reset();
    const app = fixture.componentInstance;
    app.loginForm.controls.email.markAsTouched();
    fixture.detectChanges();

    const emailRequired = fixture.debugElement.query(By.css('#email-required'));
    expect(emailRequired).toBeTruthy();
    
    const loginButton = fixture.debugElement.query(By.css('#login-button'));
    loginButton.nativeElement.click();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  }));

  it("should display email invalid message", fakeAsync( () => {
    authServiceSpy.login.calls.reset();
    const app = fixture.componentInstance;
    app.loginForm.controls.email.setValue("randomEmail")
    app.loginForm.controls.email.markAsTouched();
    fixture.detectChanges();

    const emailInvalid = fixture.debugElement.query(By.css('#email-invalid'));
    expect(emailInvalid).toBeTruthy();
    
    const loginButton = fixture.debugElement.query(By.css('#login-button'));
    loginButton.nativeElement.click();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  }));

  it("should display password required message", fakeAsync( () => {
    authServiceSpy.login.calls.reset();
    const app = fixture.componentInstance;
    app.loginForm.controls.password.markAsTouched();
    fixture.detectChanges();

    const passwordRequired = fixture.debugElement.query(By.css('#password-required'));
    expect(passwordRequired).toBeTruthy();
    
    const loginButton = fixture.debugElement.query(By.css('#login-button'));
    loginButton.nativeElement.click();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  }));

  it("should call login function", fakeAsync( () => {
    authServiceSpy.login.calls.reset();
    const app = fixture.componentInstance;
    app.loginForm.controls.email.setValue("passenger@noemail.com")
    app.loginForm.controls.password.setValue("randomPassword")
    app.loginForm.controls.email.markAsTouched();
    app.loginForm.controls.password.markAsTouched();
    fixture.detectChanges();

    const emailRequired = fixture.debugElement.query(By.css('#email-required'));
    const emailInvalid = fixture.debugElement.query(By.css('#email-invalid'));
    const passwordRequired = fixture.debugElement.query(By.css('#password-required'));
    expect(emailRequired).toBeFalsy();
    expect(emailInvalid).toBeFalsy();
    console.log(authServiceSpy);
    expect(passwordRequired).toBeFalsy();
    
    const loginButton = fixture.debugElement.query(By.css('#login-button'));
    loginButton.nativeElement.click();

    expect(authServiceSpy.login).toHaveBeenCalled();
  }));  
});
