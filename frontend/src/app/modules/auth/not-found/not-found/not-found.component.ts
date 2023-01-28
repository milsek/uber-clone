import { Component, OnInit } from '@angular/core';
import { faChevronLeft, IconDefinition } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
})
export class NotFoundComponent {
  faChevronLeft: IconDefinition = faChevronLeft;

  constructor() { }

  goHome(): void {
    window.location.href = '/';
  }

}
