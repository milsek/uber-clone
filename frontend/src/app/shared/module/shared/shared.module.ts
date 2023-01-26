import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ErrorModalComponent } from '../../components/modals/error-modal/error-modal/error-modal.component';
import { DisappearingNotificationComponent } from '../../components/modals/disappearing-notification/disappearing-notification/disappearing-notification.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RideReviewModalComponent } from '../../components/modals/ride-review-modal/ride-review-modal/ride-review-modal.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    ErrorModalComponent,
    DisappearingNotificationComponent,
    RideReviewModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FontAwesomeModule
  ],
  exports: [
    ErrorModalComponent,
    DisappearingNotificationComponent,
    RideReviewModalComponent
  ]
})
export class SharedModule { }
