import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverProfileComponent } from './driver-profile.component';

describe('DriverComponent', () => {
  let component: DriverProfileComponent;
  let fixture: ComponentFixture<DriverProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DriverProfileComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
