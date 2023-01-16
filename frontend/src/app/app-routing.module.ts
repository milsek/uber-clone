import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';
import { LoginComponent } from './modules/auth/login/login.component';
import { RegisterComponent } from './modules/auth/register/register.component';
import { MainComponent } from './modules/main/main.component';
import { DriverPageComponent } from './modules/user/profile/driver/driver.component';
import { PasswordResetComponent } from './modules/auth/password-reset/password-reset.component';

const routes: Routes = [
  { path: '', component: MainComponent },
  {
    path: 'account',
    loadChildren: () =>
      import('./modules/user/user.module').then((m) => m.UserModule),
  },
  { 
    path: 'admin',
    loadChildren: () =>
      import('./modules/admin/admin.module').then(m => m.AdminModule)
  },
  { path: 'driver/:username', component: DriverPageComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'reset-password', component: PasswordResetComponent },
  { path: '**', redirectTo: '/auth/404' },
];

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
