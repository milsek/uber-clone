import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-message-modal',
  templateUrl: './message-modal.component.html',
})
export class MessageModalComponent {
  @Input() title!: string;
  @Input() description!: string;
  @Output() closeModal: EventEmitter<void> = new EventEmitter();

  constructor() { }

  closeSelf () {
    this.closeModal.emit();
  }

}
