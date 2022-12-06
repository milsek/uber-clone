import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';
import { LoginComponent } from './modules/auth/login/login.component';
import { RegisterComponent } from './modules/auth/register/register.component';
import { MapComponent } from './modules/map/map.component';
import { DriverPageComponent } from './modules/user/profile/driver/driver.component';

const routes: Routes = [
  { path: '', component: MapComponent },
  { path: 'account', loadChildren: () => import('./modules/user/user.module').then(m => m.UserModule) },
  { path: 'driver/:username', component: DriverPageComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: '**', redirectTo: '/auth/404' },
];

@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
