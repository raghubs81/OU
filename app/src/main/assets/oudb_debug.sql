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

# ----------------------------------------------------------------------------------------------------
# Coorg Trip
# ----------------------------------------------------------------------------------------------------

insert into Trip (Name, Detail) values ("Coorg Trip", "A trip to coorg");

# Trip Users - 1-12
# ----------------------------------------------------------------------------------------------------
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Raghu"   , "Raghunandan Seshadri" ,  787, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Madhu"   , "Madhukiran Seelam"    ,  397, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Srini"   , "Srinivaslu Darmika"   ,  305, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Sanjeev" , "Sanjeev S.V."         ,  330, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Rakesh"  , "Rakesh Kashyap"       ,  787, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Hari"    , "ShreeHari Narshimha"  ,  265, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Harsha"  , "ShreeHarsha Narshimha", 1929, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Pavi"    , "Pavithra Rajagopal"   ,  643, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Abi"     , "Abirami Seelam"       ,  320, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("SriLatha", "SriLatha Uravakonda"  ,  338, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Shruthi" , "Shruthi Kumar"        ,  359, 1);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Shwetha" , "Shwetha Kumar"        ,  357, 1);

# Trip Groups - 1-3
# ----------------------------------------------------------------------------------------------------
insert into TripGroup (Name, Detail, TripId) values ("All"       , "All folks of coorg trip" , 1);
insert into TripGroup (Name, Detail, TripId) values ("BoysGroup" , "TripGroup of boys"       , 1);
insert into TripGroup (Name, Detail, TripId) values ("GirlsGroup", "TripGroup of girls"      , 1);

# Trip User Groups - Assign users to group
# ----------------------------------------------------------------------------------------------------
insert into TripUserGroup (UserId, GroupId) values (1, 1);
insert into TripUserGroup (UserId, GroupId) values (2, 1);
insert into TripUserGroup (UserId, GroupId) values (3, 1);
insert into TripUserGroup (UserId, GroupId) values (4, 1);
insert into TripUserGroup (UserId, GroupId) values (5, 1);
insert into TripUserGroup (UserId, GroupId) values (6, 1);
insert into TripUserGroup (UserId, GroupId) values (7, 1);
insert into TripUserGroup (UserId, GroupId) values (8, 1);
insert into TripUserGroup (UserId, GroupId) values (9, 1);
insert into TripUserGroup (UserId, GroupId) values (10, 1);
insert into TripUserGroup (UserId, GroupId) values (11, 1);
insert into TripUserGroup (UserId, GroupId) values (12, 1);

insert into TripUserGroup (UserId, GroupId) values (1, 2);
insert into TripUserGroup (UserId, GroupId) values (2, 2);
insert into TripUserGroup (UserId, GroupId) values (3, 2);
insert into TripUserGroup (UserId, GroupId) values (4, 2);
insert into TripUserGroup (UserId, GroupId) values (5, 2);
insert into TripUserGroup (UserId, GroupId) values (6, 2);
insert into TripUserGroup (UserId, GroupId) values (7, 2);
insert into TripUserGroup (UserId, GroupId) values (8, 3);
insert into TripUserGroup (UserId, GroupId) values (9, 3);
insert into TripUserGroup (UserId, GroupId) values (10, 3);
insert into TripUserGroup (UserId, GroupId) values (11, 3);
insert into TripUserGroup (UserId, GroupId) values (12, 3);

# Items - 1-10
# ----------------------------------------------------------------------------------------------------
insert into Item     (Summary, Detail) values ("Breakfast", "Idly at NMH");
insert into TripItem (TripId, ItemId)  values (1, 1);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (1, 1, 10000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (1, 2, 5000);
insert into ItemSharedBy (ItemId, UserId)         values (1, 1);
insert into ItemSharedBy (ItemId, UserId)         values (1, 2);
insert into ItemSharedBy (ItemId, UserId)         values (1, 3);
insert into ItemSharedBy (ItemId, UserId)         values (1, 5);

insert into Item     (Summary, Detail) values ("Lunch", "Grand lunch at MTR");
insert into TripItem (TripId, ItemId)  values (1, 2);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (2, 8, 10000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (2, 9, 25000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (2, 10, 15000);
insert into ItemSharedBy (ItemId, UserId)         values (2, 9);
insert into ItemSharedBy (ItemId, UserId)         values (2, 10);
insert into ItemSharedBy (ItemId, UserId)         values (2, 11);
insert into ItemSharedBy (ItemId, UserId)         values (2, 12);

insert into Item     (Summary, Detail) values ("Dinner", "Dinner at Pai");
insert into TripItem (TripId, ItemId)  values (1, 3);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (3, 1, 200000);
insert into ItemSharedBy (ItemId, UserId)         values (3, 1);
insert into ItemSharedBy (ItemId, UserId)         values (3, 8);

insert into Item     (Summary, Detail) values ("Bus", "Bus to coorg stop");
insert into TripItem (TripId, ItemId)  values (1, 4);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (4, 2, 350000);
insert into ItemSharedBy (ItemId, UserId)         values (4, 1);
insert into ItemSharedBy (ItemId, UserId)         values (4, 2);

insert into Item     (Summary, Detail) values ("Bus", "Auto to hotel");
insert into TripItem (TripId, ItemId)  values (1, 5);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (5, 3, 80000);
insert into ItemSharedBy (ItemId, UserId)         values (5, 3);
insert into ItemSharedBy (ItemId, UserId)         values (5, 4);

insert into Item     (Summary, Detail) values ("Tea", "Tea getting down");
insert into TripItem (TripId, ItemId)  values (1, 6);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (6, 4, 40000);
insert into ItemSharedBy (ItemId, UserId)         values (6, 4);
insert into ItemSharedBy (ItemId, UserId)         values (6, 5);
insert into ItemSharedBy (ItemId, UserId)         values (6, 6);

insert into Item     (Summary, Detail) values ("Coffee", "Coffee getting down");
insert into TripItem (TripId, ItemId)  values (1, 7);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (7, 5, 50000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (7, 7, 20000);
insert into ItemSharedBy (ItemId, UserId)         values (7, 7);
insert into ItemSharedBy (ItemId, UserId)         values (7, 8);
insert into ItemSharedBy (ItemId, UserId)         values (7, 9);

insert into Item     (Summary, Detail) values ("Coconut", "Coconut water");
insert into TripItem (TripId, ItemId)  values (1, 8);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (8, 6, 40000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (8, 7, 10000);
insert into ItemSharedBy (ItemId, UserId)         values (8, 10);
insert into ItemSharedBy (ItemId, UserId)         values (8, 11);
insert into ItemSharedBy (ItemId, UserId)         values (8, 12);

insert into Item     (Summary, Detail) values ("Ice-cream", "Mango flavored cone");
insert into TripItem (TripId, ItemId)  values (1, 9);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (9, 8, 30000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (9, 9, 20000);
insert into ItemSharedBy (ItemId, UserId)         values (9, 1);
insert into ItemSharedBy (ItemId, UserId)         values (9, 2);
insert into ItemSharedBy (ItemId, UserId)         values (9, 3);
insert into ItemSharedBy (ItemId, UserId)         values (9, 4);
insert into ItemSharedBy (ItemId, UserId)         values (9, 5);

insert into Item     (Summary, Detail) values ("Ice-cream", "Jackfruit flavored cup");
insert into TripItem (TripId, ItemId)  values (1, 10);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (10, 10, 25000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (10, 11, 35000);
insert into ItemSharedBy (ItemId, UserId)         values (10, 6);
insert into ItemSharedBy (ItemId, UserId)         values (10, 7);
insert into ItemSharedBy (ItemId, UserId)         values (10, 8);
insert into ItemSharedBy (ItemId, UserId)         values (10, 9);
insert into ItemSharedBy (ItemId, UserId)         values (10, 10);
insert into ItemSharedBy (ItemId, UserId)         values (10, 11);
insert into ItemSharedBy (ItemId, UserId)         values (10, 12);

# ----------------------------------------------------------------------------------------------------
# Dharwad Trip
# ----------------------------------------------------------------------------------------------------
insert into Trip (Name, Detail) values ("Dharwad Trip", "A trip to dharward");

# Trip Users - 13-16
# ----------------------------------------------------------------------------------------------------
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Raghu"  , "Raghunandan Seshadri", 787, 2);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Sanjeev", "Sanjeev S.V."        , 330, 2);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Pavi"   , "Pavithra Rajagopal"  , 643, 2);
insert into TripUser (NickName, FullName, ContactId, TripId) values ("Sudan"  , "Sudanesh Madhu"      , 787, 2);

# Trip Groups - 4-4
# ----------------------------------------------------------------------------------------------------
insert into TripGroup (Name, Detail, TripId) values ("All"       , "All folks of dharwad trip" , 2);

# Trip User Groups - Assign users to group
# ----------------------------------------------------------------------------------------------------
insert into TripUserGroup (UserId, GroupId) values (13, 4);
insert into TripUserGroup (UserId, GroupId) values (14, 4);
insert into TripUserGroup (UserId, GroupId) values (15, 4);
insert into TripUserGroup (UserId, GroupId) values (16, 4);

# Items - 11-13
# ----------------------------------------------------------------------------------------------------
insert into Item     (Summary, Detail) values ("Breakfast", "Idly at NMH");
insert into TripItem (TripId, ItemId)  values (2, 11);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (11, 13, 10000);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (11, 14,  5000);
insert into ItemSharedBy (ItemId, UserId)         values (11, 13);
insert into ItemSharedBy (ItemId, UserId)         values (11, 14);
insert into ItemSharedBy (ItemId, UserId)         values (11, 15);

insert into Item     (Summary, Detail) values ("Train tickets", "Tickets to Dharwad");
insert into TripItem (TripId, ItemId)  values (2, 12);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (12, 13, 300000);
insert into ItemSharedBy (ItemId, UserId)         values (12, 13);
insert into ItemSharedBy (ItemId, UserId)         values (12, 14);
insert into ItemSharedBy (ItemId, UserId)         values (12, 15);

insert into Item     (Summary, Detail) values ("Filter coffee", "Coffee at the hotel");
insert into TripItem (TripId, ItemId)  values (2, 13);
insert into ItemPaidBy   (ItemId, UserId, Amount) values (13, 14, 5000);
insert into ItemSharedBy (ItemId, UserId)         values (13, 13);
insert into ItemSharedBy (ItemId, UserId)         values (13, 14);