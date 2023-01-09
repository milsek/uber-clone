import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AppComponent } from './app.component';
import { AuthModule } from './modules/auth/auth.module';
import { AuthenticationService } from './core/authentication/authentication.service';
import { MainModule } from './modules/main/main.module';
import { UserModule } from './modules/user/user.module';
import { ChatComponent } from './modules/chat/chat.component';

@NgModule({
  declarations: [AppComponent, ChatComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FontAwesomeModule,
    AuthModule,
    MainModule,
    UserModule,
  ],
  providers: [AuthenticationService],
  bootstrap: [AppComponent],
})
export class AppModule {}
