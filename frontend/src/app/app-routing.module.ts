import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';
import { MainComponent } from './modules/main/main.component';
import { DriverPageComponent } from './modules/user/profile/driver/driver.component';

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
      import('./modules/admin/admin.module').then((m) => m.AdminModule),
  },
  {
    path: 'passenger',
    loadChildren: () =>
      import('./modules/passenger/passenger.module').then(
        (m) => m.PassengerModule
      ),
  },
  {
    path: 'drivers',
    loadChildren: () =>
      import('./modules/driver/driver.module').then((m) => m.DriverModule),
  },
  {
    path: 'reports',
    loadChildren: () =>
      import('./modules/reports/reports.module').then((m) => m.ReportsModule),
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./modules/auth/auth.module').then((m) => m.AuthModule),
  },
  { path: 'driver/:username', component: DriverPageComponent },
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
