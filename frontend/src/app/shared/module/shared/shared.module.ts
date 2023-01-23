import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ErrorModalComponent } from '../../components/modals/error-modal/error-modal/error-modal.component';
import { DisappearingNotificationComponent } from '../../components/modals/disappearing-notification/disappearing-notification/disappearing-notification.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';


@NgModule({
  declarations: [
    ErrorModalComponent,
    DisappearingNotificationComponent
  ],
  imports: [
    CommonModule,
    FontAwesomeModule
  ],
  exports: [
    ErrorModalComponent,
    DisappearingNotificationComponent
  ]
})
export class SharedModule { }
