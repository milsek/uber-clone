import { AfterContentInit, Component, OnInit } from '@angular/core';
import { AuthenticationService } from 'src/app/core/authentication/authentication.service';
import { ChatService } from 'src/app/core/http/user/chat.service';
import { SocketService } from 'src/app/core/socket/socket.service';
import { Chat } from 'src/app/shared/models/chat.model';
import { Message } from 'src/app/shared/models/message.model';
import { User } from 'src/app/shared/models/user.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
})
export class ChatComponent implements OnInit {
  numOfNewMessages: number = 0;
  indexOfSelectedChat: number = 0;
  showChat: boolean = false;
  shouldScroll: boolean = false;
  newMessage: string = '';
  receiver: string = '';
  user: User = { username: '', role: '' };
  chats: Array<Chat> = [];

  constructor(
    private chatService: ChatService,
    private authenticationService: AuthenticationService,
    private socketService: SocketService
  ) {}

  ngOnInit(): void {
    this.initChats();
  }

  initChats(): void {
    const session = this.authenticationService.getSession();
    if (!session) return;
    if (session.accountType === 'admin') {
      this.user = {
        username: 'admin',
        role: session.accountType,
      };
      this.chatService.getAllChats().then((response) => {
        this.chats = response.data;
        this.sortChats();
        this.countNumberOfUnreadMessages();
        this.receiver = this.chats[0].member.username;
      });
    } else {
      this.user = {
        username: session.username,
        role: session.accountType,
      };
      this.chatService.getUserChat(this.user.username).then((response) => {
        this.chats = [response.data];
        this.sortChats();
        this.countNumberOfUnreadMessages();
        this.receiver = 'admin';
      });
    }
    let addr = '';
    if (session.accountType === 'admin') {
      addr = '/user/admin/private';
    } else {
      addr = '/user/' + session.username + '/private';
    }
    this.onMessageRecieve(addr);
  }

  onMessageRecieve(addr: string): void {
    this.socketService.stompClient.subscribe(addr, (message: any) => {
      let messageData = JSON.parse(message.body);
      let mess = {
        sender: messageData.sender,
        content: messageData.content,
        sentDateTime: new Date(messageData.sentDateTime),
      };
      if (messageData.type === 'CHAT') {
        if (this.user.role === 'admin') {
          for (let chat of this.chats) {
            if (chat.member.username === messageData.sender) {
              chat.messages.push(mess);
            }
          }
        } else {
          this.chats[0].messages.push(mess);
        }
        this.checkNewMessage(mess.sender);
        const member = this.chats[this.indexOfSelectedChat].member.username;
        this.sortChats();
        this.indexOfSelectedChat = this.chats.indexOf(
          this.chats.filter((chat) => chat.member.username === member)[0]
        );
        this.scroll();
        if (this.showChat) {
          this.updateLastRead();
        }
      }
    });
  }

  createNewMessage(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.newMessage.trim() === '') {
      return;
    }
    let currentDate = new Date();
    let tempDate = new Date();
    tempDate.setHours(tempDate.getHours() + 1);
    this.socketService.stompClient.send(
      '/app/privateMessage',
      {},
      JSON.stringify({
        type: 'CHAT',
        sender: this.user.username,
        receiver: this.receiver,
        content: this.newMessage.trim(),
        sentDateTime: tempDate,
      })
    );
    this.chats[this.indexOfSelectedChat].messages.push({
      sender: this.user.username,
      content: this.newMessage,
      sentDateTime: currentDate,
    });
    this.newMessage = '';
    this.sortChats();
    this.indexOfSelectedChat = 0;
    this.scroll();
    this.updateLastRead();
    this.checkIfStillHasUnread();
  }

  onValueChange(event: Event): void {
    const value = (event.target as any).value;
    this.newMessage = value;
  }

  changeChat(username: string): void {
    for (let i = 0; i < this.chats.length; i++) {
      if (this.chats[i].member.username === username) {
        this.indexOfSelectedChat = i;
        this.receiver = this.chats[i].member.username;
        this.checkIfStillHasUnread();
        this.scroll();
        break;
      }
    }
  }

  changeShowChat(): void {
    this.showChat = !this.showChat;
    if (this.showChat) {
      this.scroll();
      this.checkIfStillHasUnread();
    }
  }

  checkIfStillHasUnread(): void {
    if (this.user.role !== 'admin') {
      this.numOfNewMessages = 0;
      return;
    }
    let chat = this.chats[this.indexOfSelectedChat];
    this.numOfNewMessages -= this.getNumberOfUnread(chat);
    this.updateLastRead();
  }

  updateLastRead(): void {
    let type = '';
    if (this.user.role === 'admin') {
      this.chats[this.indexOfSelectedChat].lastReadAdmin = new Date();
      type = 'admin';
    } else this.chats[this.indexOfSelectedChat].lastReadMember = new Date();
    this.chatService.updateChat(
      this.chats[this.indexOfSelectedChat].member.username,
      type
    );
  }

  getNumberOfUnread(chat: Chat): number {
    if (chat.messages.length === 0) return 0;
    let numOfUnread = 0;
    let lastRead =
      this.user.role === 'admin' ? chat.lastReadAdmin : chat.lastReadMember;
    if (typeof lastRead === 'string') {
      lastRead = new Date(lastRead);
    }
    for (let i = chat.messages.length - 1; i > 0; i--) {
      let messTime = chat.messages[i].sentDateTime;
      if (typeof messTime === 'string') {
        messTime = new Date(messTime);
      }
      if (messTime > lastRead) numOfUnread += 1;
      else break;
    }
    return numOfUnread;
  }

  countNumberOfUnreadMessages(): void {
    for (let chat of this.chats) {
      this.numOfNewMessages += this.getNumberOfUnread(chat);
    }
  }

  scroll(): void {
    setTimeout(() => {
      let objdiv = document.getElementById('chat-scroll');
      if (objdiv) objdiv!.scrollTo(0, objdiv!.scrollHeight);
    }, 5);
  }

  checkNewMessage(sender: string): void {
    if (!this.showChat) {
      this.numOfNewMessages += 1;
    } else if (this.receiver !== sender && this.user.role === 'admin') {
      this.numOfNewMessages += 1;
    } else {
      this.numOfNewMessages = 0;
    }
  }

  getTime(message: Message): string {
    message.sentDateTime = new Date(message.sentDateTime);
    return message.sentDateTime.toLocaleTimeString('en-GB', {
      hour: 'numeric',
      minute: 'numeric',
    });
  }

  sortChats(): void {
    this.chats.sort((a, b) => {
      let aDate = new Date(a.messages[a.messages.length - 1].sentDateTime);
      let bDate = new Date(b.messages[b.messages.length - 1].sentDateTime);
      return bDate.getTime() - aDate.getTime();
    });
  }
}
