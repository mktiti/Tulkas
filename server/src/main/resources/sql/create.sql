create table if not exists User(
    id       bigint identity primary key,
    name     varchar(100) not null unique,
    password varchar(100) not null
);

create table if not exists JarData(
    id      bigint identity primary key,
    data    blob(10M) not null
);

create table if not exists Game(
    id          bigint identity primary key,
    ownerId     bigint not null references User(id),
    name        varchar(100) not null unique,
    isMatch     boolean not null,
    apiJarId    bigint not null references JarData(id),
    engineJarId bigint not null references JarData(id)
);

create table if not exists Bot(
    id      bigint identity primary key,
    ownerId bigint not null references User(id),
    gameId  bigint not null references Game(id),
    name    varchar(100) not null,
    jarId   bigint not null references JarData(id),

    unique (ownerId, gameId, name)
);

create table if not exists GameLog(
    id      bigint identity primary key,
    gameId  bigint not null references Game(id),
    botAId  bigint not null references Bot(id),
    botBId  bigint null references Bot(id),
    time    timestamp(0) default current_timestamp not null,
    result  varchar(100) not null
);

create table if not exists ActorLog(
    id     bigint identity primary key,
    gameId bigint not null references GameLog(id),
    sender varchar(6) not null,
    target varchar(6) not null,
    relativeIndex bigint not null,
    message varchar(1000) not null,

    check (sender in ('TULKAS', 'ENGINE', 'BOT_A', 'BOT_B')),
    check (target in ('ENGINE', 'BOT_A', 'BOT_B'))
);