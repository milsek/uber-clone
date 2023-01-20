import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterDriverComponent } from './register-driver/register-driver.component';
import { UpdateRequestsComponent } from './update-requests/update-requests.component';

const routes: Routes = [
  { path: 'driver-registration', component: RegisterDriverComponent },
  { path: 'update-requests', component: UpdateRequestsComponent },
  { path: '**', redirectTo: '/auth/404' },
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
