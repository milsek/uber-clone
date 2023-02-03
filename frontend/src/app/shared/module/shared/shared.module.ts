import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageModalComponent } from '../../components/modals/message-modal/message-modal.component';
import { DisappearingNotificationComponent } from '../../components/modals/disappearing-notification/disappearing-notification.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RideReviewModalComponent } from '../../components/modals/ride-review-modal/ride-review-modal.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    MessageModalComponent,
    DisappearingNotificationComponent,
    RideReviewModalComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FontAwesomeModule
  ],
  exports: [
    MessageModalComponent,
    DisappearingNotificationComponent,
    RideReviewModalComponent
  ]
})
export class SharedModule { }
