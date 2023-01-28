import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GoogleLoginComponent } from './google-login/google-login.component';
import { LoginComponent } from './login/login.component';
import { NotFoundComponent } from './not-found/not-found/not-found.component';
import { PasswordResetComponent } from './password-reset/password-reset.component';
import { RegisterComponent } from './register/register.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'google-login', component: GoogleLoginComponent },
  { path: 'reset-password', component: PasswordResetComponent },
  { path: '404', component: NotFoundComponent },
];

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [ RouterModule ]
})
export class AuthRoutingModule { }
