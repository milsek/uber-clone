import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RideReportsComponent } from './ride-reports/ride-reports.component';
const routes: Routes = [
  { path: '', component: RideReportsComponent },
  { path: '**', redirectTo: '/auth/404' },
];

@NgModule({
  declarations: [],
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportsRoutingModule {}
