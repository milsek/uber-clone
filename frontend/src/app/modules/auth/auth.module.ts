import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ReactiveFormsModule } from '@angular/forms';
import { AppRoutingModule } from 'src/app/app-routing.module';
import { PasswordResetComponent } from './password-reset/password-reset.component';
import { GoogleLoginComponent } from './google-login/google-login.component';



@NgModule({
  declarations: [ RegisterComponent, LoginComponent, PasswordResetComponent, GoogleLoginComponent ],
  imports: [
    CommonModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    AppRoutingModule
  ]
})
export class AuthModule { }
