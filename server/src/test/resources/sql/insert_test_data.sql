--Users
insert into User(name, password) values ('alice',    'alice12345');
insert into User(name, password) values ('bob',      'bob');
insert into User(name, password) values ('charlie',  'paddyspub');
insert into User(name, password) values ('daniel',   'asd');
insert into User(name, password) values ('username', 'password');

--Jar
insert into JarData(data) values (X'01FF');

--Game
-- #0 - creator: alice (#0); api, engine: JarData#0
insert into Game(ownerId, name, isMatch, apiJarId, engineJarId)
    values (0, 'Alice''s duel', true, 0, 0);

-- #1 - creator: bob (#1); api, engine: JarData#0
insert into Game(ownerId, name, isMatch, apiJarId, engineJarId)
    values (1, 'Bob''s challenge', false, 0, 0);

--Bot
-- #0 - game: Alice's duel; creator: alice (#0); jar: JarData#0
insert into Bot(ownerId, gameId, name, jarId)
    values (0, 0, 'Alice''s Adventurer', 0);

-- #1 - game: Alice's duel; creator: charlie (#2); jar: JarData#0
insert into Bot(ownerId, gameId, name, jarId)
    values (2, 0, 'Charlie''s Champion', 0);

-- #2 - game: Alice's duel; creator: daniel (#3); jar: JarData#0
insert into Bot(ownerId, gameId, name, jarId)
    values (3, 0, 'Daniel''s Defender', 0);

-- #3 - game: Bob's challenge; creator: alice (#0); jar: JarData#0
insert into Bot(ownerId, gameId, name, jarId)
    values (0, 1, 'Alice''s Megaman', 0);

-- #4 - game: Bob's challenge; creator: daniel (#3); jar: JarData#0
insert into Bot(ownerId, gameId, name, jarId)
    values (3, 1, 'Daniels''s Superman', 0);

--GameLog
-- #0 - game: Alice's duel (#0), Alice's Adventurer (#0) vs Charlie's Champion (#1)
--      result: Alice won (BOT_A_WIN)
insert into GameLog(gameId, botAId, botBId, time, result)
    values (0, 0, 1, timestamp '2000-01-01 12:34:00', 'BOT_A_WIN');

-- #1 - game: Bob's challenge (#1), Daniels's Superman (#4)
--      result: 100/200
insert into GameLog(gameId, botAId, botBId, time, result)
    values (1, 4, null, timestamp '2010-01-01 12:34:00', '100/200');