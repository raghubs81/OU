create table Trip
(
   _id    integer primary key autoincrement,
   Name   text,
   Detail text
);

create table TripUser
(
   _id        integer primary key autoincrement,
   NickName   text     not null,
   FullName   text,
   ContactId  integer  not null,
   TripId     integer  not null,

   foreign key (TripId) references Trip (_id) on delete cascade
);

create table TripGroup
(
   _id      integer primary key autoincrement,
   Name     text,
   Detail   text,
   TripId   integer not null,

   foreign key (TripId) references Trip (_id) on delete cascade
);

create table TripUserGroup
(
   _id     integer primary key autoincrement,
   UserId  integer not null,
   GroupId integer not null,

   foreign key (UserId)  references TripUser  (_id) on delete cascade,
   foreign key (GroupId) references TripGroup (_id) on delete cascade
);

create table Item
(
   _id       integer primary key autoincrement,
   Summary   text,
   Detail    text
);

create table ItemSharedBy
(
   _id       integer primary key autoincrement,
   ItemId    integer not null,
   UserId    integer not null,

   foreign key (ItemId) references Item (_id) on delete cascade,
   foreign key (UserId) references TripUser (_id)
);

create table ItemPaidBy
(
   _id      integer primary key autoincrement,
   Amount   integer,
   ItemId   integer not null,
   UserId   integer not null,

   foreign key (ItemId) references Item (_id)  on delete cascade,
   foreign key (UserId) references TripUser (_id)
);

create table TripItem
(
   _id            integer primary key autoincrement,
   TripId        integer not null,
   ItemId        integer not null,

   foreign key (TripId) references Trip (_id) on delete cascade,
   foreign key (ItemId) references Item (_id) on delete cascade
);