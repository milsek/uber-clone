import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverReviewsComponent } from './driver-reviews.component';

describe('DriverReviewsComponent', () => {
  let component: DriverReviewsComponent;
  let fixture: ComponentFixture<DriverReviewsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DriverReviewsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverReviewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
