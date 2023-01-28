import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-error-modal',
  templateUrl: './error-modal.component.html',
})
export class ErrorModalComponent {
  @Input() title!: string;
  @Input() description!: string;
  @Output() closeModal: EventEmitter<void> = new EventEmitter();

  constructor() { }

  closeSelf () {
    this.closeModal.emit();
  }

}
