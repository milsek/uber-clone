import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-error-modal',
  templateUrl: './error-modal.component.html',
})
export class ErrorModalComponent implements OnInit {
  @Input() title!: string;
  @Input() description!: string;
  @Output() onClose: EventEmitter<void> = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

  close () {
    this.onClose.emit();
  }

}
