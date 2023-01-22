import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterDriverComponent } from './register-driver/register-driver.component';
import { RideRejectionRequestsComponent } from './ride-rejection-requests/ride-rejection-requests.component';
import { UpdateRequestsComponent } from './update-requests/update-requests.component';

const routes: Routes = [
  { path: 'driver-registration', component: RegisterDriverComponent },
  { path: 'update-requests', component: UpdateRequestsComponent },
  { path: 'ride-rejection-requests', component: RideRejectionRequestsComponent },
  { path: '**', redirectTo: '/auth/404' },
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
