import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ReactiveFormsModule } from '@angular/forms';
import { PasswordResetComponent } from './password-reset/password-reset.component';
import { GoogleLoginComponent } from './google-login/google-login.component';
import { AuthRoutingModule } from './auth-routing.module';
import { NotFoundComponent } from './not-found/not-found/not-found.component';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';

@NgModule({
  declarations: [ RegisterComponent, LoginComponent, PasswordResetComponent, GoogleLoginComponent, NotFoundComponent ],
  imports: [
    CommonModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    AuthRoutingModule,
    SharedModule
  ]
})
export class AuthModule { }
