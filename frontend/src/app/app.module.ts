import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AppComponent } from './app.component';
import { AuthModule } from './modules/auth/auth.module';
import { AuthenticationService } from './core/authentication/authentication.service';
import { MainModule } from './modules/main/main.module';
import { UserModule } from './modules/user/user.module';
import { AdminModule } from './modules/admin/admin.module';
import { ChatComponent } from './modules/chat/chat.component';
import { SocketService } from './core/socket/socket.service';

@NgModule({
  declarations: [AppComponent, ChatComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FontAwesomeModule,
    AuthModule,
    MainModule,
    UserModule,
    AdminModule,
  ],
  providers: [
    AuthenticationService,
    SocketService,
    {
      provide: APP_INITIALIZER,
      useFactory: (socketService: SocketService) => () => socketService.initWS(),
      deps: [SocketService],
      multi: true
    }
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
