import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverEditComponent } from './driver-edit.component';

describe('DriverEditComponent', () => {
  let component: DriverEditComponent;
  let fixture: ComponentFixture<DriverEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DriverEditComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
