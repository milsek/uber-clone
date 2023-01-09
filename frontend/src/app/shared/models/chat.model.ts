import { Message } from './message.model';
import { User } from './user.model';

export interface Chat {
  member: User;
  messages: Array<Message>;
  lastReadAdmin: Date;
  lastReadMember: Date;
}
