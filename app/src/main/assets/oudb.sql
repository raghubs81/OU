create table Trip
(
   _id    integer primary key autoincrement,
   Name   text,
   Detail text
);

create table TripUser
(
   _id        integer primary key autoincrement,
   NickName   text    not null,
   FirstName  text,
   LastName   text,
   Mobile     text    not null,
   Email      text,
   TripId     integer not null,

   foreign key (TripId) references Trip (_id)
);

create table TripGroup
(
   _id      integer primary key autoincrement,
   Name     text,
   Detail   text,
   TripId   integer not null,

   foreign key (TripId) references Trip (_id)
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

   foreign key (ItemId) references Item (_id),
   foreign key (UserId) references TripUser (_id)
);

create table ItemPaidBy
(
   _id      integer primary key autoincrement,
   Amount   integer,
   ItemId   integer not null,
   UserId   integer not null,

   foreign key (UserId) references TripUser (_id),
   foreign key (ItemId) references Item (_id)
);

create table TripItem
(
   _id            integer primary key autoincrement,
   TripId        integer not null,
   ItemId        integer not null,

   foreign key (TripId) references Trip (_id),
   foreign key (ItemId) references Item (_id)
);

# ----------------------------------------------------------------------------------------------------
# Coorg Trip
# ----------------------------------------------------------------------------------------------------

insert into Trip (Name, Detail) values ("Coorg Trip", "A trip to coorg");

# Trip Users - 1-12
# ----------------------------------------------------------------------------------------------------
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Raghu"   , "Raghunandan" , "Seshadri" , "9880526030", "raghubs81@gmail.com"    , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Madhu"   , "Madhukiran"  , "Seelam"   , "7897838942", "madhu.seelam@gmail.com" , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Srini"   , "Srinivaslu"  , "Darmika"  , "8985989495", "smd@gmail.com"          , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Sanjeev" , "Sanjeev"     , "S.V."      , "5367894963", "sanjeevi@gmail.com"    , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Rakesh"  , "Rakesh"      , "Kashyap"   , "3462467646", "rakesh@gmail.com"      , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Hari"    , "ShreeHari"   , "Narshimha" , "1243434334", "hari@gmail.com"        , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Harsha"  , "ShreeHarsha" , "Narshimha" , "6756434334", "harsha@gmail.com"      , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Pavi"    , "Pavithra"    , "Rajagopal" , "9632767348", "pavi@gmail.com"        , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Abi"     , "Abirami"     , "Seelam"    , "7897838942", "madhu.seelam@gmail.com", 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("SriLatha", "SriLatha"    , "Uravakonda", "4354976475", "srilatha@gmail.com"    , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Shruthi" , "Shruthi"     , "Kumar"     , "5274566565", "shru.chilli@gmail.com" , 1);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Shwetha" , "Shwetha"     , "Kumar"     , "4133416547", "swetha@gmail.com"      , 1);

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
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Raghu"  , "Raghunandan", "Seshadri"  , "9880526030", "raghubs81@gmail.com"   , 2);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Sanjeev", "Sanjeev"    , "S.V."      , "5367894963", "sanjeevi@gmail.com"    , 2);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Pavi"   , "Pavithra"   , "Rajagopal" , "9632767348", "pavi@gmail.com"        , 2);
insert into TripUser (NickName, FirstName, LastName, Mobile, Email, TripId) values ("Sudan"  , "Sudanesh"   , "Madhu"     , "5666767348", "sudan@gmail.com"       , 2);

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




